# lein-vertx

A leiningen plugin to ease clojure vertx verticle development.

## Usage

The current stable release of the plugin is `0.1.0`. Put
`[lein-vertx "0.1.0"]` into the `:plugins` vector of your
`project.clj` or in your `~/.lein/profiles.clj` to use it.

Then add `:vertx {:main your-app.core/some-init-fn}` to
`project.clj`. When you call `lein vertx run`, the plugin will
synthesize a verticle .clj file that calls that function, and launch
vertx with that verticle. See the sample-app for an example.

To use the latest development version, check out his repo and install
`0.2.0-SNAPSHOT` with `lein install`.

## License

Copyright © 2012 Isaiah P.

Distributed under the Eclipse Public License, the same as Clojure.
