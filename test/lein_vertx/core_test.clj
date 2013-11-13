(ns lein-vertx.core-test
  (:use [clojure.test]
        [lein-vertx.core]
        [pandect.core])
  (:require [clojure.java.io :as io]))

(deftest test-verticlize
  (is (= "VertxHttpServer.clj" (verticlize "vertx.http.server"))))

(deftest test-entry-points
  (let [app-root (io/file (io/resource "app-root"))
        entries (entry-points {:source-paths "src" :compile-path "target"} (.getAbsolutePath app-root))]
    (is (= '("core.txt" "mod.json") (map #(.getName %) entries)))))

(deftest test-writezip
  (let [outfile (str "target/mod.zip")
        app-root (io/file (io/resource "app-root"))]
    (write-zip outfile (entry-points {:source-paths "src" :compile-path "target"} (.getAbsolutePath app-root)))
    (is (= "540a07289e6cd3b17134648ee7673617" (md5-file outfile)))))
