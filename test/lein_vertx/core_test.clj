(ns lein-vertx.core-test
  (:use [clojure.test]
        [lein-vertx.core]
  (:require [clojure.java.io :as io]))

(deftest test-verticlize
  (is (= "VertxHttpServer.clj" (verticlize "vertx.http.server"))))
