(ns pulsar-kit.electron.processes.main.browser-window.web-contents
  (:require devtools.core))

;https://www.electronjs.org/docs/latest/api/web-contents

(def electron (js/require "electron"))
(def BrowserWindow (.getCurrentWindow (.-remote electron)))
(def WebContents (.-webContents BrowserWindow))

#!==============================================================================
#! devtools api

(defn open-devtools
  "options {?Map} -
    :mode {?string} - Opens the devtools with specified dock state, can be left, right, bottom, undocked, detach. Defaults to last used dock state. In undocked mode it's possible to dock back. In detach mode it's not.
    :activate {?boolean} - Whether to bring the opened devtools window to the foreground. The default is true.
    :title {?string} - A title for the DevTools window (only in undocked or detach mode)."
  ([]
   (open-devtools {}))
  ([{:keys [mode activate] :as opts}]
   (.openDevTools WebContents (clj->js opts))))

(defn close-devtools [] (.closeDevTools WebContents))

(defonce _*installed-devtools? (delay (do (devtools.core/install!) true)))

(defn ^:dynamic *on-devtools-open* [] @_*installed-devtools?)
(defn ^:dynamic *on-devtools-close* [])
(defn ^:dynamic *on-devtools-focus* [])

(defonce _
  (do
    (.on WebContents "devtools-opened" *on-devtools-open*)
    (.on WebContents "devtools-closed" *on-devtools-close*)
    (.on WebContents "devtools-focus" *on-devtools-focus*)))
