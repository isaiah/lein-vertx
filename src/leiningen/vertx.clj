(ns leiningen.vertx
  (:use [leiningen.help :only [help-for]]
        [clojure.java.shell])
  (:require [leiningen.core.eval :as eval]
            [leiningen.core.main :as main]
            [clojure.pprint :as pprint]
            [lein-vertx.core :as core])
  (:import (java.io FileNotFoundException File)))

(defn run [project & args]
  (apply core/invoke-vertx project "run" (core/write-main project) args))

(defn vertx
  "Leiningen plugin to run vertx verticle."
  ([project]
     (println (help-for "vertx")))
  ([project subtask & args]
     (case subtask
       "run" (apply run project args))))
