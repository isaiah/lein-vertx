(defproject sample-app "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [leiningen "2.1.3"]]
  :plugins [[lein-vertx "0.2.0-SNAPSHOT"]]
  :vertx {:main sample-app.core/init
          :author "Isaiah Peng"
          :keywords ["bread", "toasting", "toasters", "nuclear"]
          :developers ["Toby Crawley"]}
  :repl-options { :init (do (require 'leiningen.core.project)
                        (def project
                             (leiningen.core.project/read)))})
