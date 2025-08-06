(ns pulsar-kit.ppm
  (:require [jsonista.core :as j]
            [babashka.process :as process]))

(defn list-packages []
  (j/read-value
    (:out (process/shell {:out :string :err :inherit} "ppm ls --json"))
    j/keyword-keys-object-mapper))

(defn link-package [package-path]
  (:out (process/shell {:out :string :err :inherit} (str "ppm link " package-path))))

(defn unlink-package [package-path]
  (:out (process/shell {:out :string :err :inherit} (str "ppm link " package-path))))

(defn remove-package [package-path]
  (:out (process/shell {:out :string :err :inherit} (str "ppm remove " package-path))))

(defn disable-package [package-path]
  (:out (process/shell {:out :string :err :inherit} (str "ppm disable " package-path))))

(defn enable-package [package-path]
  (:out (process/shell {:out :string :err :inherit} (str "ppm enable " package-path))))
