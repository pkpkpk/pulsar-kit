(ns pulsar-kit.environment.notifications
  (:require [malli.core :as m]))

(defn list-notifications [] (js/atom.notifications.getNotifications))

(defn clear! [] (js/atom.notifications.clear))

(def notification-opts-schema
  (m/schema
   [:map
    [:notification {:optional true
                    :doc "callbacks added to notification after creation"}
     [:map
      [:onDidDisplay {:optional true} fn?]
      [:onDidDismiss {:optional true} fn?]]]
    [:buttons {:optional true}
     [:sequential
      {:doc "An Array of Object where each Object has the following options"}
      [:map
       [:className
        {:optional true
         :doc "String a class name to add to the button’s default class name (btn btn-success)."}
        :string]
       [:onDidClick
        {:optional true
         :doc "Function callback to call when the button has been clicked. The context will be set to the NotificationElement instance."}
        fn?]
       [:text
        {:optional true
         :doc "String inner text for the button"}
        :string]]]]
    [:description {:optional true
                   :doc "A Markdown String containing a longer description about the notification. By default, this will not preserve newlines and whitespace when it is rendered."}
     :string]
    [:detail {:optional true
              :doc "A plain-text String containing additional details about the notification. By default, this will preserve newlines and whitespace when it is rendered."}
     :string]
    [:dismissable
     {:optional true
      :doc "A Boolean indicating whether this notification can be dismissed by the user. Defaults to false"}
     :boolean]
    [:icon
     {:optional true
      :doc "A String name of an icon from Octicons to display in the notification header. Defaults to 'check'"}
     :string]]))

(defn add-notification
  [method msg {{:keys [onDidDisplay onDidDismiss]} :notification :as opts}]
  (let [opts (clj->js (m/coerce notification-opts-schema opts))
        notification (.call method js/atom.notifications msg opts)]
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
