(ns pulsar-kit.electron.processes.main.browser-window)

;https://www.electronjs.org/docs/latest/api/browser-window

(def electron (js/require "electron"))
(def BrowserWindow (.getCurrentWindow (.-remote electron)))
