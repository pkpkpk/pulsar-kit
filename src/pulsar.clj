(ns pulsar
  (:require [babashka.cli :as cli]
            [babashka.fs :as fs]
            [babashka.process :as process]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [shadow.cljs.devtools.api :as shadow])
  (:import (java.lang ProcessHandle)))

#_(do (require :reload 'pulsar) (in-ns 'pulsar) (use 'clojure.repl))

(def HOME (System/getProperty "user.home"))

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

(defn launch []
  (reset! *instance
          (process/process
            {:dir HOME
             #_ #_ :env {"ATOM_DISABLE_SHELLING_OUT_FOR_ENVIRONMENT" "true"}}
            (pulsar-exec-path)
            (str "--executed-from=" HOME)
            "--no-sandbox")))

(defn kill! []
  (when (alive?)
    (process/destroy-tree @*instance)
    (reset! *instance nil)))