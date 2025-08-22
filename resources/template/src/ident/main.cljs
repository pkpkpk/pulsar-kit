(ns {{IDENT}}.main
  (:require [pulsar-kit.electron.processes.main.browser-window.web-contents :as web-contents]
            [pulsar-kit.environment.commands :as commands]
            [pulsar-kit.environment.notifications :as notifications]
            ["path" :as path]))

(def command-tuples
  [
   ["atom-workspace" "{{IDENT}}:clear-console" (.bind js/console.clear js/console)]
  ])


(defonce worker (js/Worker. (.resolve path js/window.__dirname "{{PACKAGE_PATH}}/target/{{PATH_IDENT}}/worker/worker.js")))

(defn ^:dev/before-load before-load [& args])
(defn ^:dev/after-load after-load [& args] (notifications/add-info "did reload"))

(defn -main [& args] (js/console.log "{{IDENT}}.main/-main()"))

(defn activate []
  (try
    (do
      (js/console.log "activating {{IDENT}} package...")
      (commands/register! command-tuples)
      (web-contents/open-devtools {:activate false})
      (notifications/add-success "{{IDENT}} package ready!" {:dismissable true}))
    (catch js/Error e
      (notifications/add-fatal (str "Error during {{IDENT}} package activation: " (ex-message e)))
      (js/console.error e))))