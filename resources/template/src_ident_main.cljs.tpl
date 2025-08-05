(ns {{IDENT}}.main
  (:require [pulsar-kit.environment.notifications :as notifications]
            [pulsar-kit.electron.processes.main.browser-window.web-contents :as web-contents]))

(def path (js/require "path"))
(defonce worker (js/Worker. (.resolve path js/window.__dirname "{{HOME}}/target/{{IDENT}}/worker/worker.js")))

(defn ^:dev/before-load before-load [& args])
(defn ^:dev/after-load after-load [& args] (notifications/add-info "did reload"))

(defn -main [& args] (js/console.log "{{IDENT}}.main/-main()"))

(defn activate []
  (try
    (do
      (js/console.log "activating {{IDENT}} package...")
      (web-contents/open-devtools {:activate false})
      (notifications/add-success "{{IDENT}} package ready!" {:dismissable true}))
    (catch js/Error e
      (notifications/add-fatal (str "Error during {{IDENT}} package activation: " (ex-message e)))
      (js/console.error e))))