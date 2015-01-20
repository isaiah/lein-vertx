(ns leiningen.vertx
  (:use [leiningen.help :only [help-for]]
        [clojure.java.shell])
  (:require [leiningen.core.eval :as eval]
            [leiningen.core.main :as main]
            [clojure.pprint :as pprint]
            [lein-vertx.core :as core])
  (:import (java.io FileNotFoundException File)))

(defn run
  "Run the main function specified from command line '-m' or under [:vertx :main] in project.clj "
  [project main & args]
  (apply core/invoke-vertx project "run"
         (core/write-main project (symbol main))
         args))

(defn repl
  "Start vertx repl on a random port"
  [project & args]
  (apply run project "vertx.repl/start" args))

(defn vertx
  "Leiningen plugin to run vertx verticle."
   {:help-arglists '([subtask [args...]])
    :subtasks [#'run #'repl #'core/buildmod]}
  ([project]
     (println (help-for "vertx")))
  ([project subtask & [flag & args :as all-args]]
     (case subtask
       "run" (cond (or
                     (= flag ":main")
                     (= flag "-m")) (if (first args)
                                      (apply run project args)
                                      (main/abort "Option -m requires a qualified function name."))
                   (-> project :vertx :main) (apply run project (-> project :vertx :main) all-args)
                   :else (main/abort (str "No main provided.\n"
                     "Specify a fully qualified function name on the command-line,\n"
                     "or under [:vertx :main] in project.clj.")))
       "repl" (apply repl project all-args)
       "buildmod" (core/buildmod project (-> project :vertx :main) all-args)
       (println (help-for "vertx")))))
