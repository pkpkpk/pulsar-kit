```clojure
com.github.pkpkpk/pulsar-kit {:git/sha "ba69431a77fbffea3d7723477ff37d04d6b3cc82"}
```

This is toolkit for injecting shadow-cljs into the [pulsar](https://github.com/pulsar-edit/pulsar) runtime and driving the editor from the repl. Pulsar-kit only shaves yaks and provides bindings; the package it produces is yours to modify and use as you choose.

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
(require 'pulsar-kit '[shadow.cljs.devtools.api :as shadow])

(pulsar-kit/create-package "projects/demo") ;; creates projects/demo directory, populates & links it to ppm

(pulsar-kit/launch :demo) ;; <-- starts shadow repl and launches pulsar with derived build-id

(pulsar-kit/shutdown) ;; <-- stops shadow and kills pulsar

(shadow/repl :demo)
```

+ Notice that shadow logs the nrepl port and ALSO puts `.nrepl-port` file to disk.
  - (**not recommended**) in same terminal, you can connect to the cljs-repl with `(shadow/repl :demo)`,
  - (**recommended**) in a separate terminal you can connect to the `.nrepl-port` and start the repl with the same command
+ You first time start devtools you need to go into settings and 'enable custom formatters', a checkbox towards the bottom of the first settings page
  - there is also a custom dark theme if you are into that kind of thing
