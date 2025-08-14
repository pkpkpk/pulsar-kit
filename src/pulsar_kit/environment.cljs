(ns pulsar-kit.environment
  (:require [malli.core :as m]
            [pulsar-kit.electron.processes.main.browser-window.web-contents :as wc]
            pulsar-kit.environment.workspace))

(def open-params-schema
  [:map
   [:pathsToOpen
    {:doc "An Array of String paths to open."}
    [:sequential :string]]
   [:newWindow
    {:doc "true to always open a new window instead of reusing existing windows depending on the paths to open."}
    :boolean]
   [:devMode :boolean]
   [:safeMode
    {:doc "true to open the window in safe mode. Safe mode prevents all packages installed to ~/.pulsar/packages from loading"}
    :boolean]])

(defn open
  "open a new window with given options
   Calling without options will prompt to pick a file/folder to open in the new window."
  ([](js/atom.open))
  ([params]
   (js/atom.open (clj->js (m/coerce open-params-schema params)))))

(defn current-window [] (js/atom.getCurrentWindow))

(defn center
  "move current window to center of screen"
  [] (js/atom.center))

(defn focus [] (js/atom.focus))

(defn show [] (js/atom.show))

(defn hide [] (js/atom.hide))

(defn reload [] (js/atom.reload))

(defn restart-application [] (js/atom.restartApplication))

(defn close [] (js/atom.close))

(defn maximized? [] (js/atom.isMaximized))

(defn full-screen? [] (js/atom.isFullScreen))

(defn set-full-screen [] (js/atom.setFullScreen))

(defn toggle-full-screen [] (js/atom.toggleFullScreen))

(defn get-load-settings []
  (-> (js/atom.getLoadSettings) (js->clj :keywordize-keys true)))

(defn get-size
  "Get the size of current window in pixels #js{:width 1920 :height 1080}"
  [] (js/atom.getSize))

(defn set-size
  "Set the size of current window in pixels"
  [width height] (js/atom.setSize width height))

(defn get-position
  "get position of current window in pixels as #js{:x 0 :y 0}"
  [] (js/atom.getPosition))

(defn set-position
  "set position of current window"
  [x y] (js/atom.setPosition x y))

(defn pick-folder
  "cb is called with array of strings, each an absolute path to a folder"
  [cb] (js/atom.pickFolder (m/coerce fn? cb)))

; ::onWillThrowError(callback)
; ::onDidThrowError(callback)
; ::whenShellEnvironmentLoaded(callback)

#!------------------------------------------------------------------------------

(def confirm-options-schema
  (m/schema
   [:map
    [:message {:doc "text to display"} :string]
    [:detailedMessage {:optional true} :string]
    [:buttons {:optional true} [:sequential :string]]]))

(defn confirm
  "blocks when cb not provided."
  ([options]
   (let [opts (m/coerce confirm-options-schema options)
         res (js/atom.confirm (clj->js opts))]
     (if-let [btns (get opts :buttons)]
       (nth btns res)
       res)))
  ([options cb]
   (let [opts (m/coerce confirm-options-schema options)]
     (js/atom.confirm (clj->js opts)
       (fn [res]
         (if-let [btns (get opts :buttons)]
           (cb (nth btns res))
           (cb res)))))))

#!------------------------------------------------------------------------------

(def open-devtools wc/open-devtools)

(def close-devtools wc/close-devtools)

(defn toggle-devtools [] (js/atom.toggleDevtools))
