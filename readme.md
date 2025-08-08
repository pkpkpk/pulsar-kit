```clojure
com.github.pkpkpk/pulsar-kit {:git/sha "02a440d8f526e369fefc3393648e89cb30bd1264"}
```

This is toolkit for injecting a shadow-cljs into the [pulsar](https://github.com/pulsar-edit/pulsar) runtime and driving the editor from the repl. Pulsar-kit only shaves yaks and provides bindings; the package it produces is yours to modify and use as you choose.

### Installation
1) have [pulsar installed](https://github.com/pulsar-edit/pulsar/releases)
2) call `(pulsar-kit/create-package 'path/to/project/<ident>')`
   + Package creation only requires a path to the directory, where the name of the project is derived from the filepath.
   + This identity is used both to generate code and produce the keyword build-id used to start the shadow-cljs repl, so choose something that is simple and a valid clojure symbol
3) (from home) launch pulsar with a repl via `(pulsar-kit/launch :<ident>)`
   + [shadow-cljs](https://shadow-cljs.github.io/) frequently reads config from a *shadow-cljs.edn* in the cwd... you are restricted to running your repl to where the desired config file can be read.
   + By default, pulsar-kit will link the generated shadow-cljs.edn to your home directory, which has more utility than running from within the package

### Example

```clojure
(require 'pulsar-kit)

(pulsar-kit/create-package "projects/demo") ;; creates projects/demo directory, populates & links it to ppm

(pulsar-kit/launch :demo) ;; <-- starts shadow repl and launches pulsar with derived build-id

(pulsar-kit/shutdown) ;; <-- stops shadow and kills pulsar

```
