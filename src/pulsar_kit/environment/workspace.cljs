(ns pulsar-kit.environment.workspace
  (:refer-clojure :exclude [replace])
  (:require [malli.core :as m]))

#!------------------------------------------------------------------------------
#! Opening

(defn open [uri opts])

(defn hide [item-or-uri])

(defn toggle [item-or-uri])

(defn create-item-for-uri [uri])

(defn reopen-item [])

(defn add-opener [opener])

#!------------------------------------------------------------------------------
#! TextEditors

(defn observe-text-editors
  "Invoke the given callback with all current and future text editors in the workspace.
   @param {!function<TextEditor>} called with current and future text editors
   where TextEditor is present in ::getTextEditors at the time of subscription or that is added at some later time.
   @return {!Disposable} where disposable.dispose() can be called to unsubscribe."
  [cb] (js/atom.workspace.observeTextEditors cb))

(defn observe-active-text-editor
  "Invoke the given callback with the current active text editor (if any), with all future active text editors, and when there is no longer an active text editor.
   @param  {!function<?TextEditor>} Function called when the active text editor changes; receives the active TextEditor or undefined.
   @return {!Disposable} where disposable.dispose() can be called to unsubscribe."
  [cb] (js/atom.workspace.observeActiveTextEditor cb))

(defn on-did-add-text-editor
  "Invoke the given callback when a text editor is added to the workspace.
   @param  {!function<{textEditor: TextEditor, pane: Pane, index: number}>} Function called with the event describing the added editor.
   @return {!Disposable} where disposable.dispose() can be called to unsubscribe."
  [cb] (js/atom.workspace.onDidAddTextEditor cb))

(defn on-did-change-active-text-editor
  "Invoke the given callback when a text editor becomes the active text editor and when there is no longer an active text editor.
   @param  {!function<?TextEditor>} Function called when the active text editor changes; receives the active TextEditor or undefined.
   @return {!Disposable} where disposable.dispose() can be called to unsubscribe."
  [cb] (js/atom.workspace.onDidChangeActiveTextEditor cb))

(defn ^boolean text-editor?
  "Check whether the given object is a TextEditor.
   @param  {!Object} Object to check.
   @return {boolean}"
  [obj] (js/atom.workspace.isTextEditor obj))

(defn create-text-editor
  "create a new (detached) text-editor
   @return {!TextEditor}"
  [] (js/atom.workspace.buildTextEditor))

(defn list-text-editors
  "Get all text editors in the workspace, if they are pane items.
   @return {Array<TextEditor>}"
  [] (js/atom.workspace.getTextEditors))

(defn active-text-editor
  "Get the workspace center’s active item if it is a TextEditor.
   @return {?TextEditor}"
  [] (js/atom.workspace.getActiveTextEditor))

#!------------------------------------------------------------------------------
#! Panes

(defn observe-pane-items
  "Invoke the given callback with all current and future panes items in the workspace.
   @param  {!function<Item>} Function to be called with current and future pane items.
   where an item that is present in ::getPaneItems at the time of subscription or that is added at some later time.
   @return {!Disposable} where disposable.dispose() can be called to unsubscribe."
  [cb] (js/atom.workspace.observePaneItems cb))

(defn list-pane-items
  "Get all pane items in the workspace."
  [] (js/atom.workspace.getPaneItems))

(defn active-pane-item
  "Get the active Pane’s active item."
  [] (js/atom.workspace.getActivePaneItem))

(defn list-panes
  "Get all panes in the workspace."
  [] (js/atom.workspace.getPanes))

(defn active-pane-container
  "Get the most recently focused pane container."
  [] (js/atom.workspace.getActivePaneContainer))

(defn active-pane
  "Get the active Pane.
   @return {?Pane}"
  [] (js/atom.workspace.getActivePane))

(defn activate-next-pane!
  "Make the next pane active."
  [] (js/atom.workspace.activateNextPane))

(defn activate-previous-pane!
  "Make the previous pane active."
  [] (js/atom.workspace.activatePreviousPane))

(defn pane-container-for-uri
  "Get the first pane container that contains an item with the given URI.
   @return {?(Dock|WorkspaceCenter)}"
  [uri] (js/atom.workspace.paneContainerForURI uri))

(defn pane-container-for-item
  "Get the first pane container that contains the given item.
   @return {?(Dock|WorkspaceCenter)}"
  [item] (js/atom.workspace.paneContainerForItem item))

(defn pane-for-uri
  "Get the pane that contains an item with the given URI.
   @return {?Pane}"
  [uri] (js/atom.workspace.paneForURI uri))

(defn pane-for-item
  "Get the pane that contains the given item.
   @return {?Pane}"
  [item] (js/atom.workspace.paneForItem item))

#!------------------------------------------------------------------------------
#! Pane Dock Locations

(defn center
  "Get the WorkspaceCenter at the center of the editor window.
   @return {!WorkspaceCenter}"
  [] (js/atom.workspace.getCenter))

(defn left-dock
  "Get the Dock on the left side of the editor window.
   @return {!Dock}"
  [] (js/atom.workspace.getLeftDock))

(defn right-dock
  "Get the Dock on the right side of the editor window.
   @return {!Dock}"
  [] (js/atom.workspace.getRightDock))

(defn bottom-dock
  "Get the Dock at the bottom of the editor window.
   @return {!Dock}"
  [] (js/atom.workspace.getBottomDock))

#!------------------------------------------------------------------------------
#! Panels

(defn list-bottom-panels
  "Get an Array of all the panel items at the bottom of the editor window.
   @return {Array<Panel>}"
  [] (js/atom.workspace.getBottomPanels))

(defn list-left-panels
  "Get an Array of all the panel items to the left of the editor window.
   @return {Array<Panel>}"
  [] (js/atom.workspace.getLeftPanels))

(defn list-right-panels
  "Get an Array of all the panel items to the right of the editor window.
   @return {Array<Panel>}"
  [] (js/atom.workspace.getRightPanels))

(defn list-top-panels
  "Get an Array of all the panel items at the top of the editor window.
   @return {Array<Panel>}"
  [] (js/atom.workspace.getTopPanels))

(defn list-header-panels
  "Get an Array of all the panel items in the header.
   @return {Array<Panel>}"
  [] (js/atom.workspace.getHeaderPanels))

(defn list-footer-panels
  "Get an Array of all the panel items in the footer.
   @return {Array<Panel>}"
  [] (js/atom.workspace.getFooterPanels))

(defn list-modal-panels
  "Get an Array of all modal panels.
   @return {Array<Panel>}"
  [] (js/atom.workspace.getModalPanels))

(def add-panel-options-schema
  (m/schema
   [:map
    [:item
     {:doc "Your panel content: DOM element, jQuery element, or a model with a view via ViewRegistry::addViewProvider."}
     some?]
    [:visible
     {:doc "Boolean; false to initially hide (default: true)"
      :optional true}
     :boolean]
    [:priority
     {:doc "Number; stacking order. Lower = closer to window edges (default: 100)"
      :optional true}
     number?]
    [:autoFocus
     {:doc "Modal-only: true to auto-manage focus, or an Element to focus"
      :optional true}
     [:or :boolean :any]]]))

(defn add-bottom-panel
  "Adds a panel item to the bottom of the editor window.
   @param  {Object} options {item, visible?, priority?}
   @return {!Panel}"
  [options]
  (js/atom.workspace.addBottomPanel
    (clj->js (m/coerce add-panel-options-schema (or options {})))))

(defn add-left-panel
  "Adds a panel item to the left of the editor window.
   @param  {Object} options {item, visible?, priority?}
   @return {!Panel}"
  [options]
  (js/atom.workspace.addLeftPanel
    (clj->js (m/coerce add-panel-options-schema (or options {})))))

(defn add-right-panel
  "Adds a panel item to the right of the editor window.
   @param  {Object} options {item, visible?, priority?}
   @return {!Panel}"
  [options]
  (js/atom.workspace.addRightPanel
    (clj->js (m/coerce add-panel-options-schema (or options {})))))

(defn add-top-panel
  "Adds a panel item to the top of the editor window.
   @param  {Object} options {item, visible?, priority?}
   @return {!Panel}"
  [options]
  (js/atom.workspace.addTopPanel
    (clj->js (m/coerce add-panel-options-schema (or options {})))))

(defn add-header-panel
  "Adds a panel item to the header.
   @param  {Object} options {item, visible?, priority?}
   @return {!Panel}"
  [options]
  (js/atom.workspace.addHeaderPanel
    (clj->js (m/coerce add-panel-options-schema (or options {})))))

(defn add-footer-panel
  "Adds a panel item to the footer.
   @param  {Object} options {item, visible?, priority?}
   @return {!Panel}"
  [options]
  (js/atom.workspace.addFooterPanel
    (clj->js (m/coerce add-panel-options-schema (or options {})))))

(defn add-modal-panel
  "Adds a panel item as a modal dialog.
   @param  {Object} options {item, visible?, priority?, autoFocus?}
   @return {!Panel}"
  [options]
  (js/atom.workspace.addModalPanel
   (clj->js (m/coerce add-panel-options-schema (or options {})))))

(defn panel-for-item
  "Get the Panel associated with the given item.
   @param  {!Item} Item the panel contains
   @return {?Panel}"
  [item] (js/atom.workspace.panelForItem item))

#!------------------------------------------------------------------------------
#! Search & Replace

(def scan-options-schema
  (m/schema
    [:map
     [:paths
      {:doc "Array of glob patterns to search within" :optional true}
      [:sequential :string]]
     [:onPathsSearched
      {:doc "Function periodically called with the number of paths searched" :optional true}
      fn?]
     [:leadingContextLineCount
      {:doc "Lines of leading context to include in results" :optional true}
      number?]
     [:trailingContextLineCount
      {:doc "Lines of trailing context to include in results" :optional true}
      number?]]))

(defn scan
  "Performs a search across all files in the workspace.
   @param  {!RegExp} regex
   @param  {Object}  options {paths, onPathsSearched, leadingContextLineCount, trailingContextLineCount}
   @param  {!function} iterator Function callback on each file found.
   @return {!Promise}"
  [regex options iterator]
  (js/atom.workspace.scan
    regex
    (clj->js (m/coerce scan-options-schema (or options {})))
    iterator))

(defn replace
  "Performs a replace across the specified files in the project.
   @param  {!RegExp}  regex
   @param  {!string}  replacement-text
   @param  {Array<string>} file-paths
   @param  {!function} iterator Function called per file with replacements: (fn [{:keys [filePath replacements]}])
   @return {!Promise}"
  [regex replacement-text file-paths iterator]
  (js/atom.workspace.replace
    regex
    replacement-text
    (clj->js file-paths)
    iterator))

