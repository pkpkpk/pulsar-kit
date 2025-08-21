(ns pulsar-kit.environment.commands
  (:refer-clojure :exclude [find])
  (:require [malli.core :as m]))

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

   @return {Disposable} on which `.dispose()` can be called to remove the command handler(s)."
  [target commandName listener])

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
   (find (.getElement (.-workspace js/atom))))
  ([target]
   (.findCommands (.-commands js/atom) #js {:target target})))


(defn dispatch [])

(defn on-will-dispatch [])

(defn on-did-dispatch [])