(ns pulsar-kit
  (:require [babashka.cli :as cli]
            [babashka.fs :as fs]
            [babashka.process :as process]
            [borkdude.rewrite-edn :as r]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.repl.deps :as deps]
            [clojure.string :as string]
            [malli.core :as m]
            [pulsar-kit.ppm :as ppm]
            [shadow.build :as build]
            [shadow.build.data :as data]
            [shadow.cljs.devtools.api :as shadow]
            [shadow.cljs.devtools.config :as config]
            [shadow.cljs.devtools.server :as shadow-server])
  (:import (java.lang ProcessHandle)))

(def HOME (System/getProperty "user.home"))

(defn home? [] (= HOME (System/getProperty "user.dir")))

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

(defn yes-or-no-p [prompt]
  (print (str prompt " (y/n) "))
  (flush)
  (let [response (read-line)]
    (boolean (re-matches #"(?i)y(?:es)?|n(?:o)?" (string/trim response)))))

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

(defn path-ident [ident] (string/replace ident "-" "_"))

(defn link-shadow-cljs-dot-edn [package-path]
  (assert (home?) "must run from $HOME directory")
  (let [f (io/file "shadow-cljs.edn")]
    (if (.exists f)
      (throw (Exception. "TODO reconcile existing shadow-cljs.edn"))
      (fs/create-sym-link f (io/file package-path "shadow-cljs.edn")))))

(defn template-file [& parts]
  (apply io/file (into [(io/resource "template")] parts)))

(defn copy-interpolated [package-path template dst]
  (let [ident (extract-ident package-path)]
    (spit dst (-> (slurp template)
                  (string/replace "{{PACKAGE_PATH}}" package-path)
                  (string/replace "{{PATH_IDENT}}" (path-ident ident))
                  (string/replace "{{IDENT}}" ident)
                  (string/replace "{{HOME}}" HOME)))))

(defn copy-package-templates [package-path]
  (assert (home?))
  (let [d (io/file package-path)]
    (when-not (.exists d)
      (println "creating package in " (.getPath d))
      (let [ident             (extract-ident package-path)
            copy-interpolated (partial copy-interpolated ident)
            deps              (io/file d "deps.edn")
            main              (io/file package-path "src" (path-ident ident) "main.cljs")
            worker            (io/file package-path "src" (path-ident ident) "worker.cljs")
            boot              (io/file package-path "lib" (str (path-ident ident) ".js"))]
        (io/make-parents deps)
        (copy-interpolated (template-file "shadow-cljs.edn") (io/file d "shadow-cljs.edn"))
        (copy-interpolated (template-file "deps.edn") deps)
        (copy-interpolated (template-file "package.json") (io/file d "package.json"))
        (io/make-parents boot)
        (copy-interpolated (template-file "lib" "ident.js") boot)
        (io/make-parents main)
        (copy-interpolated (template-file "src" "ident" "main.cljs") main)
        (copy-interpolated (template-file "src" "ident" "worker.cljs") worker)))))

(defn ensure-package-is-on-classpath [package-path])

#!----------------------------------------------------------------------------------------------------------------------
#! Public API

#_
(defn install-package
  "given extant package directory, setup shadow-cljs.edn & link to pulsar"
  [package-path])

#_(defn update-package [package-path])

(defn create-package [package-path]
  (copy-package-templates package-path)
  (link-shadow-cljs-dot-edn package-path)
  (ppm/link-package package-path)
  (ensure-package-is-on-classpath package-path))

(defn delete-package [package-path]
  (fs/delete-tree package-path))

#!-----------------------------------------------------------------------------

(defn warnings
  "takes state result from shadow/compile*"
  [{:keys [build-sources] :as state}]
  (not-empty
    (into {}
          (map
            (fn [key]
              (let [info (data/get-source-by-id state key)]
                (when-let [warnings (seq (build/enhance-warnings state info))]
                  [key warnings]))))
          build-sources)))

(defn start-shadow
  ([build-id] ;;accept package-path too?
   (shadow-server/start!)
   (shadow/compile* build-id {}) ;; we want a throw on errors
   (shadow/watch (keyword build-id))))

(defn stop-shadow [build-id]
  (shadow/stop-worker build-id)
  (shadow-server/stop!))

#!-----------------------------------------------------------------------------

(defn launch [build-id] ;;accept package-path too?
  (start-shadow build-id)
  ;; TODO we should be able to launch on build failure to fix issues
  ;;  --- static bootloader script should loop try
  ;;  --- expose manual reload api in client
  (launch-pulsar))

(defn relaunch-pulsar []
  (kill-pulsar!)
  (launch-pulsar))

(defn shutdown []
  (shadow-server/stop!)
  (kill-pulsar!))

(comment
  (do (require :reload 'pulsar-kit) (in-ns 'pulsar-kit) (use 'clojure.repl))
  (ensure-home-shadow-cljs-dot-edn "Projects/nb3")

  (io/resource "deku/main.cljs")
  (deps/add-lib 'pkpkpk/deku {:local/root "Projects/deku"})
  (io/resource "deku/main.cljs")
  )