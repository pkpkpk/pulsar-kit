(ns pulsar-kit
  (:require [babashka.cli :as cli]
            [babashka.fs :as fs]
            [babashka.process :as process]
            [borkdude.rewrite-edn :as r]
            [clojure.java.io :as io]
            [clojure.repl.deps :as deps]
            [clojure.string :as string]
            [pulsar-kit.ppm :as ppm]
            [shadow.build :as build]
            [shadow.build.data :as data]
            [shadow.cljs.devtools.api :as shadow]
            [shadow.cljs.devtools.server :as shadow-server])
  (:import (java.lang ProcessHandle)))

(def HOME (System/getProperty "user.home"))

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

(defn yes-or-no-p [prompt]
  (loop []
    (print (str prompt " (y/n) "))
    (flush)
    (let [response (string/trim (read-line))]
      (case (.toLowerCase response)
        "y" true
        "yes" true
        "n" false
        "no" false
        (recur)))))

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

(defn extract-ident [package-path]
  (let [ident (.getName (io/file package-path))]
    (assert (ident-safe? ident))
    (assert (string/ends-with? package-path (name ident)))
    ident))

(defn path-ident [ident] (string/replace ident "-" "_"))

(defn link-shadow-cljs-dot-edn [package-path]
  (let [f (io/file "shadow-cljs.edn")]
    (if (.exists f)
      (throw (Exception. "TODO reconcile existing shadow-cljs.edn"))
      (fs/create-sym-link f (io/file package-path "shadow-cljs.edn")))))

(defn template-file [& parts]
  (apply io/file (into [(io/resource "template")] parts)))

(defn copy-interpolated [package-path template dst]
  (let [ident (extract-ident package-path)]
    (spit dst (-> (slurp template)
                  (string/replace "{{PACKAGE_PATH}}" (.getAbsolutePath (io/file package-path)))
                  (string/replace "{{PATH_IDENT}}" (path-ident ident))
                  (string/replace "{{IDENT}}" ident)))))

(defn copy-package-templates [package-path]
  (let [d (io/file package-path)]
    (when-not (.exists d)
      (println "Creating package in " (.getAbsolutePath d))
      (let [ident             (extract-ident package-path)
            copy-interpolated (partial copy-interpolated package-path)
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

(defn find-deps-edn []
  (if-let [env-config (System/getenv "CLJ_CONFIG")]
    (io/file env-config)
    (loop [dir (io/file (System/getProperty "user.dir"))]
      (let [f (io/file dir "deps.edn")]
        (cond
          (.exists f) f
          (nil? (.getParentFile dir))
          (let [dot-clojure (io/file HOME ".clojure" "deps.edn")]
            (and (.exists dot-clojure)
                 dot-clojure))
          :else (recur (.getParentFile dir)))))))

(defn ensure-package-is-on-classpath [package-path]
  (let [ident (extract-ident package-path)
        main (str (path-ident ident) "/main.cljs")]
    (when (nil? (io/resource main))
      (let [lib (symbol "pulsar-kit" ident)
            coordinate {:local/root package-path}]
        (println "Adding package " lib " to classpath...")
        (deps/add-lib lib coordinate)
        (assert (io/resource main) (str "failed to add dep " {lib coordinate}))
        (when-let [deps-file (find-deps-edn)]
          (when (yes-or-no-p (str "\nThe '" ident "' package has been dynamically added to the current class-path.\n"
                                  "The deps.edn file for this scope is '" (.getAbsolutePath deps-file) "' \n"
                                  "Would you like to add the dependency into that file?"))
            (let [nodes (r/parse-string (slurp deps-file))
                  deps' (str (r/assoc-in nodes [:deps lib] coordinate))]
              (spit deps-file deps'))))))))

#!----------------------------------------------------------------------------------------------------------------------
#! Public API

(defonce *instance (atom nil))

(defn alive? [] (some-> @*instance process/alive?))

(defn launch-pulsar []
  (reset! *instance
          (process/process
            {:dir HOME
             #_#_:env {"ATOM_DISABLE_SHELLING_OUT_FOR_ENVIRONMENT" "true"}}
            (pulsar-exec-path)
            (str "--executed-from=" HOME)
            "--no-sandbox"
            #_"-d")))

(defn kill-pulsar! []
  (when (alive?)
    (process/destroy-tree @*instance)
    (reset! *instance nil)))

(defn start-shadow
  ([build-id] ;;accept package-path too?
   (let [build-id (keyword build-id)
         worker-id (keyword (str (name build-id) ".worker"))]
     (shadow-server/start!)
     (shadow/compile! build-id {}) ;; using compile! because we want a throw on errors
     (shadow/compile! worker-id  {})
     (shadow/watch build-id)
     (shadow/watch worker-id))))

(defn stop-shadow [build-id]
  (let [build-id  (keyword build-id)
        worker-id (keyword (str (name build-id) ".worker"))]
    (shadow/stop-worker build-id)
    (shadow/stop-worker worker-id)
    (shadow-server/stop!)))

(defn launch [build-id] ;;accept package-path too?
  (start-shadow build-id)
  ;; TODO we should be able to launch on build failure to fix issues
  ;;  --- static bootloader script should loop try
  ;;  --- expose manual reload api in client
  (launch-pulsar))

(defn create-package [package-path]
  (let [ident (extract-ident package-path)]
    (copy-package-templates package-path)
    (ppm/link package-path)
    (ensure-package-is-on-classpath package-path)
    (link-shadow-cljs-dot-edn package-path)
    (println "Package creation complete with build key '" (keyword ident) "'")))

(defn link-package
  "given extant package directory, setup shadow-cljs.edn & link to pulsar"
  [package-path]
  (let [ident (extract-ident package-path)]
    (assert (io/file package-path "shadow-cljs.edn"))
    (ppm/link package-path)
    (link-shadow-cljs-dot-edn package-path)
    (ensure-package-is-on-classpath package-path)))

(defn unlink-package [package-path]
  (let [ident (extract-ident package-path)]
    (assert (io/file package-path "shadow-cljs.edn"))
    (ppm/unlink package-path)
    (let [cfg (io/file HOME "shadow-cljs.edn")]
      (when (and (.exists cfg)
                 (fs/sym-link? cfg)
                 (= (str (fs/real-path cfg))
                    (.getAbsolutePath (io/file package-path "shadow-cljs.edn"))))
        (fs/delete cfg)))))

(defn linked? [package-path])

(defn purge-package [package-path]
  (when-let [deps-file (find-deps-edn)]
    (let [ident (extract-ident package-path)
          nodes (r/parse-string (slurp deps-file))
          lib (symbol "pulsar-kit" ident)]
      (when (r/get-in nodes [:deps lib])
        (spit deps-file (str (r/update-in nodes [:deps] dissoc lib))))))
  (let [cfg (io/file HOME "shadow-cljs.edn")]
    (when (and (.exists cfg)
               (fs/sym-link? cfg)
               (= (str (fs/real-path cfg))
                  (.getAbsolutePath (io/file package-path "shadow-cljs.edn"))))
      (fs/delete cfg)))
  (ppm/unlink package-path)
  (fs/delete-tree package-path))

(defn relaunch-pulsar []
  (kill-pulsar!)
  (launch-pulsar))

(defn shutdown []
  (shadow-server/stop!)
  (kill-pulsar!))

(comment
  (do (require :reload 'pulsar-kit) (in-ns 'pulsar-kit) (use 'clojure.repl))
  )