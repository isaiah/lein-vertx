(ns leiningen.vertx
  (:use [leiningen.help :only [help-for]]
        [clojure.java.shell])
  (:require [leiningen.core.eval :as eval]
            [leiningen.core.main :as main]
            [clojure.pprint :as pprint])
  (:import (java.io FileNotFoundException File)))


(defn run
  [project & args]
  (let [vert (symbol (first args))
        project (dissoc project :prep-tasks)]
    (try
      (eval/eval-in-project project
                            `(doseq [v# (vals (ns-interns '~vert))]
                               (when-let [vert-class# (:verticle (meta v#))]
                                 (println "vertx run" vert-class#)
                                 (let [rt# (with-sh-dir ~(:compile-path project) (sh "vertx" "run" vert-class#))]
                                   (println (:out rt#))
                                   (println (:err rt#)))))
                            `(try
                               (compile '~vert)
                               (require 'clojure.java.shell)
                               (catch FileNotFoundException _#)))
      (catch clojure.lang.ExceptionInfo e
        (main/exit)))))

(defn vertx
  "Leiningen plugin to run vertx verticle."
  ([project]
     (println help-for "vertx"))
  ([project subtask & args]
     (case subtask
       "run" (apply run project args))))
