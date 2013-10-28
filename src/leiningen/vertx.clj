(ns leiningen.vertx
  (:use [leiningen.help :only [help-for]]
        [clojure.java.shell])
  (:require [leiningen.core.eval :as eval]
            [leiningen.core.main :as main]
            [clojure.pprint :as pprint]
            [lein-vertx.core :as core])
  (:import (java.io FileNotFoundException File)))

(defn run [project main & args]
  (if main
    (apply core/invoke-vertx project "run"
           (core/write-main project (symbol main))
           args)
    (main/abort (str "No main provided.\n"
                     "Specify a fully qualified function name on the command-line,\n"
                     "or under [:vertx :main] in project.clj."))))

(defn vertx
  "Leiningen plugin to run vertx verticle."
  ([project]
     (println (help-for "vertx")))
  ([project subtask & args]
     (case subtask
       "run" (if (first args)
               (apply run project args)
               (apply run project (-> project :vertx :main) args)))))
