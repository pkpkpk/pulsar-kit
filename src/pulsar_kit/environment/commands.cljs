(ns pulsar-kit.environment.commands
  "Associates listener functions with commands in a context-sensitive way using
   CSS selectors. You can access a global instance of this class via
   atom.commands, and commands registered there will be presented in the command
   palette.

   The global command registry facilitates a style of event handling known as
   event delegation that was popularized by jQuery. Atom commands are expressed
   as custom DOM events that can be invoked on the currently focused element via
   a key binding or manually via the command palette. Rather than binding
   listeners for command events directly to DOM nodes, you instead register
   command event listeners globally on atom.commands and constrain them to
   specific kinds of elements with CSS selectors.

  Command names must follow the namespace:action pattern, where namespace will
  typically be the name of your package, and action describes the behavior of
  your command. If either part consists of multiple words, these must be
  separated by hyphens. E.g. awesome-package:turn-it-up-to-eleven.
  All words should be lowercased.

  As the event bubbles upward through the DOM, all registered event listeners
  with matching selectors are invoked in order of specificity. In the event of a
  specificity tie, the most recently registered listener is invoked first.
  This mirrors the “cascade” semantics of CSS. Event listeners are invoked in
  the context of the current DOM node, meaning this always points at
  event.currentTarget. As is normally the case with DOM events, stopPropagation
  and stopImmediatePropagation can be used to terminate the bubbling process and
  prevent invocation of additional listeners."
  (:refer-clojure :exclude [find])
  (:require [malli.core :as m]
            [pulsar-kit.exports.event-kit :as ek]))

(defn add
  "Register one or more command listeners on a selector or DOM element.
   @param {(string|Element)} target - A CSS selector or a DOM element.
    - If a selector, the command(s) are globally associated with all matching elements.
      (The `,` combinator is not supported.)
    - If a DOM element, the command(s) are scoped to that element only.
   @param {string} [commandName] The name of the command to handle (e.g. `user:insert-date`).

  @param {(Function|Object)} [listener]
    A listener to handle the event.
    - If a Function, it is called when the command is invoked on a matching element.
    - If an Object, it must have a `didDispatch` method, which will be called instead.

    The listener function is invoked with:
    - `this`: the matching DOM element
    - `event`: a standard DOM `Event`. Call `stopPropagation` or `stopImmediatePropagation` to stop bubbling.

    Listener objects may also define metadata properties used by `atom.commands.findCommands`:
    - `displayName {string}`: Override the generated display name.
    - `description {string}`: Provide detailed info for consumers.
    - `hiddenInCommandPalette {boolean}`: If true, hides the command from the palette unless explicitly shown.
      Useful for registering many commands that are not intended for palette use.

    @param {Object} [commands]
    An object mapping command names (e.g. `user:insert-date`) to listener functions.
    Use this form when registering multiple commands on the same target.

    @return {Disposable}"
  [target commandName listener]
  (js/atom.commands.add target commandName listener))

(defn find
  "Find all registered commands matching a target element.

   Walks up the DOM from `target` toward the root, collecting commands from:
   - inline listeners bound directly to nodes
   - selector-based listeners that match each ancestor

   Deduplicates by command *name* across the ancestor chain (first name wins), but if multiple
   inline listeners with the same name are attached to the *same* node, all of their descriptors
   are returned.

   @param {Element} target - node to start the lookup from.
     - Defaults to the workspace element.
   @return {Array<CommandDescriptor>} where descriptor includes:
     - `name {string}` (e.g. \"user:insert-date\")
     - `displayName {string}` (e.g. \"User: Insert Date\")
     - plus any metadata supplied at registration (`description`, `tags`, etc.)."
  ([]
   (find (js/atom.workspace.getElement)))
  ([target]
   (js/atom.commands.findCommands #js {:target target})))

(defn dispatch
  "Public: Simulate the dispatch of a command on a DOM node.

   Useful for testing when you want to simulate invoking a command on a detached
   DOM node. Otherwise the DOM node must be attached to the document so the
   event bubbles up to the root.

   @param {Element} target       The DOM node at which to start bubbling the command.
   @param {string} commandName   The name of the command to dispatch.
   @param {*} [detail]           Optional detail payload, exposed as `event.detail`.

   @return {boolean} true if a listener handled the command."
  ([commandName]
   (js/atom.commands.dispatch (js/atom.workspace.getElement) commandName nil))
  ([target commandName]
   (js/atom.commands.dispatch target commandName nil))
  ([target commandName detail]
   (js/atom.commands.dispatch target commandName detail)))

(defn on-will-dispatch
  "Public: Register a callback before dispatching each command event.

   @param {Function} cb  Invoked with the `Event` that will be dispatched.
   @return {Disposable}  Call `.dispose()` to unsubscribe."
  [cb] (js/atom.commands.onWillDispatch cb))

(defn on-did-dispatch
  "Public: Register a callback after dispatching each command event.

   @param {Function} cb  Invoked with the `Event` that was dispatched.
   @return {Disposable}  Call `.dispose()` to unsubscribe."
  [cb] (js/atom.commands.onDidDispatch cb))

(defn removal
  "Remove a command listener or registration.
   Accepts:
    - a Disposable → calls `.dispose`
    - a string/keyword → bluntly removes all listeners for that command
    - a map with :selector/:command → removes selector-based listener(s)
    - a map with :element/:command → removes inline listener(s) for that element"
  [arg]
  (cond
    (and (some? arg) (fn? (.-dispose ^js arg)))
    (do (.dispose ^js arg) true)

    (or (string? arg) (keyword? arg))
    (do (goog.object/remove js/atom.commands.registeredCommands (name arg))
        (goog.object/remove js/atom.commands.selectorBasedListenersByCommandName (name arg))
        (goog.object/remove js/atom.commands.inlineListenersByCommandName (name arg))
        true)

    (and (map? arg) (contains? arg :selector) (contains? arg :command))
    (do
      (goog.object/remove js/atom.commands.selectorBasedListenersByCommandName (name (:command arg)))
      true)

    (and (map? arg) (contains? arg :element) (contains? arg :command))
    (if-let [wm (goog.object/get js/atom.commands.inlineListenersByCommandName (name (:command arg)))]
      (do
        (.delete wm (:element arg))
        true)
      false)

    :else
    (throw (ex-info "removal-dispatch: unsupported argument" {:arg arg}))))

#!------------------------------------------------------------------------------

(defonce *registry (atom {})) ; command-name -> Disposable

(def command-tuple-schema
  (m/schema
   [:tuple
    [:string {:title "Selector"
              :description "CSS selector string, e.g. 'atom-workspace'"}]
    [:string {:title "Command Name"
              :description "Command label, e.g. 'nb3:clear-console'"}]
    [:or
     [fn? {:title "Listener Function"
           :description "Event listener fn taking the DOM event"}]
     [:map
      [:didDispatch [fn? {:title "Dispatch Function"
                          :description "Function called with the event"}]]]]]))

(defn- _register! [tuples]
  (let [cd (ek/composite-disposable)]
    (doseq [[selector command f] tuples]
      (when-let [old (get @*registry command)]
        (.dispose ^js old))
      (let [wrapped (fn [ev] (f ev))      ;; stable fn for hot-reload
            d       (add selector command wrapped)]
        (swap! *registry assoc command d)
        (.add cd d)))
    cd))

(defn register!
  "Install a vector of [selector command f]. Returns a CompositeDisposable.
   Re-registering the same command replaces the old disposable."
  [tuple-or-tuples]
  (if (m/validate command-tuple-schema tuple-or-tuples)
    (_register! [tuple-or-tuples])
    (_register! (m/coerce [:sequential command-tuple-schema] tuple-or-tuples))))

(defn- _unregister! [k]
  (or (when-let [d (get @*registry k)]
        (.dispose ^js d)
        (swap! *registry dissoc k)
        true)
      false))

(defn unregister!
  "Unregister a command by name"
  [command-tuple-or-tuples]
  (if (string? command-tuple-or-tuples)
    [(_unregister! command-tuple-or-tuples)]
    (if (m/validate command-tuple-schema command-tuple-or-tuples)
      [(_unregister! (second command-tuple-or-tuples))]
      (if (m/validate [:sequential command-tuple-schema] command-tuple-or-tuples)
        (mapv _unregister! (map second command-tuple-or-tuples))
        (throw (ex-info "unsupported arg" {:arg command-tuple-or-tuples}))))))

(defn unregister-all! []
  (doseq [[_ d] @*registry] (.dispose ^js d))
  (reset! *registry {}))
