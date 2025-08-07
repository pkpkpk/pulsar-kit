```clojure
com.github.pkpkpk/pulsar-kit {:git/sha f8fe52e41e057783b6e5e94aa0674fbe124032ab}
```

This is toolkit for injecting a shadow-cljs into the [pulsar](https://github.com/pulsar-edit/pulsar) runtime and driving the editor runtime from the repl. Pulsar-kit only shaves yaks and provides bindings; the package it produces is yours to modify and use as you choose.

### Installation
1) have [pulsar installed](https://github.com/pulsar-edit/pulsar/releases)
2) (from home) call `(pulsar-kit/create-package 'path/to/project/<ident>')`
3) (from home) launch pulsar with a repl via `(pulsar-kit/launch :<ident>)`

+ Package creation only requires a path to the directory, where the name of the project is derived from the base name of the filepath. This identity is used both to generate code and identity the build-id in the generated
+ [shadow-cljs](https://shadow-cljs.github.io/) frequently reads config from a *shadow-cljs.edn* in the cwd. This means you are restricted in where you run your repl to where the desired config file can be read. By default, pulsar-kit will link the generated shadow-cljs.edn to your home directory, which has more utility that running from within the package

### Example

```clojure
(require 'pulsar-kit)

(pulsar-kit/create-package "projects/demo") ;; creates projects/demo directory, populates & links it to ppm

(pulsar-kit/launch :demo) ;; <-- starts shadow repl and launches pulsar with derived build-id

(pulsar-kit/shutdown) ;; <-- stops shadow and kills pulsar

```
