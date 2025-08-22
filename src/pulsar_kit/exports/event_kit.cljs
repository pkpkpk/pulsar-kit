(ns pulsar-kit.exports.event-kit)

(def _atom (js/require "atom")) ;; event-kit is exported, we cannot require it

; https://flight-manual.atom-editor.cc/api/v1.63.1/Disposable/
(defn disposable []
  (new (.-Disposable _atom)))

; https://flight-manual.atom-editor.cc/api/v1.63.1/CompositeDisposable/
(defn composite-disposable []
  (new (.-CompositeDisposable _atom)))

;; https://flight-manual.atom-editor.cc/api/v1.63.1/Emitter/
(defn emitter []
  (new (.-Emitter _atom)))
