(defproject sample-app "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [io.vertx/vertx-core "2.0.1-final"]
                 [io.vertx/vertx-platform "2.0.1-final"]
                 ]
  :plugins [[lein-vertx "0.1.0-SNAPSHOT"]]
  :vertx {:main-fn sample-app.core/init})
