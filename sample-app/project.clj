(defproject sample-app "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :plugins [[lein-vertx "0.1.0-SNAPSHOT"]]
  :profiles {:provided
             {:dependencies [[io.vertx/clojure-api "0.2.0"]]}}
  :vertx {:main-fn sample-app.core/init})
