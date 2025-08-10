(ns pulsar-kit.ppm
  (:refer-clojure :exclude [remove list])
  (:require [jsonista.core :as j]
            [babashka.process :as process]))

(defn help
  ([]
   (:out (process/shell {:out :string :err :inherit} (str "ppm -h"))))
  ([cmd]
   (:out (process/shell {:out :string :err :inherit} (str "ppm " cmd " -h")))))

(defn list []
  (j/read-value
    (:out (process/shell {:out :string :err :inherit} "ppm ls --json"))
    j/keyword-keys-object-mapper))

(defn link [package-path]
  (:out (process/shell {:out :string :err :inherit} (str "ppm link " package-path))))

(defn unlink [package-path]
  (:out (process/shell {:out :string :err :inherit} (str "ppm unlink " package-path))))

(defn remove [package-path]
  (:out (process/shell {:out :string :err :inherit} (str "ppm remove " package-path))))

(defn disable [package-path]
  (:out (process/shell {:out :string :err :inherit} (str "ppm disable " package-path))))

(defn enable [package-path]
  (:out (process/shell {:out :string :err :inherit} (str "ppm enable " package-path))))

(defn view [package-path]
  (:out (process/shell {:out :string :err :inherit} (str "ppm view " package-path))))

(defn install [package]
  (:out (process/shell {:out :string :err :inherit} (str "ppm install " package))))
