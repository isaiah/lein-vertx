(ns lein-vertx.core-test
  (:use [clojure.test]
        [lein-vertx.core]))

(deftest test-verticlize
  (is (= "VertxHttpServer" (verticlize "vertx.http.server"))))
