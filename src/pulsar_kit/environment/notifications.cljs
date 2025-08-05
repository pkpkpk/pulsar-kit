(ns pulsar-kit.environment.notifications
  (:require [malli.core :as m]))

(defn list-notifications [] (js/atom.notifications.getNotifications))

(defn clear! [] (js/atom.notifications.clear))

(defn add-notification
  [method msg {{:keys [onDidDisplay onDidDismiss]} :notification :as opts}]
  (let [notification (if (or onDidDisplay onDidDismiss)
                       (.call method js/atom.notifications msg (clj->js opts))
                       (.call method js/atom.notifications msg (clj->js opts)))]
    (some->> onDidDisplay (.onDidDisplay notification))
    (some->> onDidDismiss (.onDidDismiss notification))
    notification))

(defn add-info
  ([msg]
   (add-info msg {}))
  ([msg opts]
   (add-notification js/atom.notifications.addInfo msg opts)))

(defn add-success
  ([msg]
   (add-success msg {}))
  ([msg opts]
   (add-notification js/atom.notifications.addSuccess msg opts)))

(defn add-warning
  ([msg]
   (add-warning msg {}))
  ([msg opts]
   (add-notification js/atom.notifications.addWarning msg opts)))

(defn add-error
  ([msg]
   (add-error msg {}))
  ([msg opts]
   (add-notification js/atom.notifications.addError msg opts)))

(defn add-fatal
  ([msg]
   (add-fatal msg {}))
  ([msg opts]
   (add-notification js/atom.notifications.addFatalError msg opts)))
