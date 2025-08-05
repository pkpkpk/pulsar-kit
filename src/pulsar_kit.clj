(ns pulsar-kit
  (:require [babashka.cli :as cli]
            [babashka.fs :as fs]
            [babashka.process :as process]
            [borkdude.rewrite-edn :as r]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as string]
            [jsonista.core :as j]
            [malli.core :as m]
            [shadow.cljs.devtools.api :as shadow]
            [shadow.cljs.devtools.config :as config]
            [shadow.cljs.devtools.server :as shadow-server])
  (:import (java.lang ProcessHandle)))

(def HOME (System/getProperty "user.home"))

(defn home? []
  (= HOME (System/getProperty "user.dir")))

(defonce *instance (atom nil))

(defn pulsar-exec-path []
  (let [os (System/getProperty "os.name")
        env-path (System/getenv "PULSAR_PATH")]
    (cond
      (.startsWith os "Mac")
      (let [base (or env-path
                     "/Applications/Pulsar.app")
            bin  (io/file base "Contents/MacOS/Pulsar")]
        (if (.canExecute ^java.io.File bin)
          (.getAbsolutePath ^java.io.File bin)
          (throw (ex-info "Pulsar binary not found on macOS" {:checked bin}))))

      (.startsWith os "Linux")
      (let [base (or env-path "/opt/Pulsar")
            bin  (io/file base "pulsar")]
        (if (.canExecute ^java.io.File bin)
          (.getAbsolutePath ^java.io.File bin)
          (throw (ex-info "Pulsar binary not found on Linux" {:expected bin}))))

      :else
      (throw (Exception. "unsupported OS")))))

(defn alive? [] (some-> @*instance process/alive?))

(defn launch-pulsar []
  (reset! *instance
          (process/process
            {:dir HOME
             #_ #_ :env {"ATOM_DISABLE_SHELLING_OUT_FOR_ENVIRONMENT" "true"}}
            (pulsar-exec-path)
            (str "--executed-from=" HOME)
            "--no-sandbox"
            #_"-d")))

(defn kill-pulsar! []
  (when (alive?)
    (process/destroy-tree @*instance)
    (reset! *instance nil)))

#!-----------------------------------------------------------------------------
#! PPM

(defn list-packages []
  (j/read-value
    (:out (process/shell {:out :string :err :inherit} "ppm ls --json"))
    j/keyword-keys-object-mapper))

;; todo init, link, unlink, etc

#!-----------------------------------------------------------------------------

(defn ident-safe? [s]
  (try
    (let [k (keyword s)
          s' (name k)
          sym (symbol s)
          s'' (name sym)]
      (and (simple-keyword? k)
           (simple-symbol? sym)
           (= s s')
           (= s s'')))
    (catch Exception _ false)))

(def target (io/file HOME "target"))

(defn extract-ident [package-path]
  (let [ident (.getName (io/file package-path))]
    (assert (ident-safe? ident))
    (assert (string/ends-with? package-path (name ident)))
    ident))

(defn main-cfg [ident]
  {:target           :node-script
   :output-dir       (str (.getAbsolutePath (io/file target ident "main")))
   :output-to        (str (.getAbsolutePath (io/file target ident "main" "main.js")))
   :main             (symbol (str ident ".main/-main"))
   :devtools         {:reload-strategy :recompile-dependents
                      :hud             true}
   :compiler-options {:infer-externs true}
   :pulsar           true})

(defn worker-cfg [ident]
  {:target           :browser
   :output-dir       (str (.getAbsolutePath (io/file target ident "worker")))
   :output-to        (str (.getAbsolutePath (io/file target ident "worker" "worker.js")))
   :compiler-options {:infer-externs true}
   :devtools         {:reload-strategy :recompile-dependents}
   :pulsar           true
   :modules {:worker {:init-fn (symbol (str ident ".worker/-main"))
             :web-worker true}}})

(defn ensure-home-shadow-cljs-dot-edn [package-path]
  (assert (home?) "must run from $HOME directory")
  (let [ident           (extract-ident package-path)
        src             (str (io/file package-path "src"))
        build-id        (keyword ident)
        worker-build-id (keyword (str ident ".worker"))
        f               (io/file "shadow-cljs.edn")
        nominal-cfg     {:deps         {:aliases [build-id]}
                         :source-paths [src]
                         :builds       {build-id (main-cfg ident)
                                        worker-build-id (worker-cfg ident)}}]
    (if (.exists f)
      (do
        ;; slurp, modify w/ rewrite-edn, spit
        (throw (Exception. "TODO verify existing shadow-cljs.edn")))
      (spit "shadow-cljs.edn" (with-out-str (pprint/pprint nominal-cfg))))))

(defn ensure-package [package-path]
  (let [ident (extract-ident package-path)
        d (io/file package-path)]
    (when-not (.exists d)
      ;; make-parents
      ;; 1) spit src core & worker
      ;; 2) spit package.json
      ;; 3) spit lib/index.js
      ;;;4) spit deps.edn
      ;;;5) spit resources
      ;;;6) ppm link
      )))

#!----------------------------------------------------------------------------------------------------------------------
#!
#! Public API
#!

(defn create-package [package-path])

(defn update-package [package-path])

(defn start-shadow
  ([build-id] ;;accept package-path too?
   (shadow-server/start!)
   (shadow/watch (keyword build-id))))

(defn launch [build-id] ;;accept package-path too?
  (start-shadow build-id)
  ;; TODO we should be able to launch on build failure to fix issues
  ;;  --- static bootloader script should loop try
  ;;  --- expose manual load
  (launch-pulsar))

(defn shutdown [])

(comment
  (do (require :reload 'pulsar-kit) (in-ns 'pulsar-kit) (use 'clojure.repl))

  (ensure-home-shadow-cljs-dot-edn "Projects/nb3")
  )