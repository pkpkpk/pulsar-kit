(ns pulsar-kit.exports.event-kit)

(def _atom (js/require "atom")) ;; event-kit is exported, we cannot require it

(defn composite-disposable []
  (new (.-CompositeDisposable _atom)))