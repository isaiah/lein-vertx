# lein-vertx

A leiningen plugin to ease clojure vertx verticle development.

## Usage

Put `[lein-vertx "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your
`project.clj` or in your `~/.lein/profiles.clj`.

Then add `:vertx {:main-fn your-app.core/some-init-fn}` to
`project.clj`. When you call `lein vertx run`, the plugin will
synthesize a verticle .clj file that calls that function, and launch
vertx with that verticle. See the sample-app for an example.

## License

Copyright © 2012 Isaiah P.

Distributed under the Eclipse Public License, the same as Clojure.
