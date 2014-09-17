(ns leiningen.vertx
  (:use [leiningen.help :only [help-for]]
        [clojure.java.shell])
  (:require [leiningen.core.eval :as eval]
            [leiningen.core.main :as main]
            [clojure.pprint :as pprint]
            [lein-vertx.core :as core])
  (:import (java.io FileNotFoundException File)))

(defn run 
  "Run the main function specified from command line or under [:vertx :main] in project.clj"
  [project main & args]
  (if main
    (apply core/invoke-vertx project "run"
           (core/write-main project (symbol main))
           args)
    (main/abort (str "No main provided.\n"
                     "Specify a fully qualified function name on the command-line,\n"
                     "or under [:vertx :main] in project.clj."))))

(defn repl
  "Start vertx repl on a random port"
  [project]
  (run project "vertx.repl/start"))

(defn vertx
  "Leiningen plugin to run vertx verticle."
   {:help-arglists '([subtask [args...]])
    :subtasks [#'run #'repl #'core/buildmod]}
  ([project]
     (println (help-for "vertx")))
  ([project subtask & args]
     (case subtask
       "run" (if (first args)
               (apply run project args)
               (apply run project (-> project :vertx :main) args))
       "repl" (repl project)
       "buildmod" (core/buildmod project (-> project :vertx :main) args)
       (println (help-for "vertx")))))
