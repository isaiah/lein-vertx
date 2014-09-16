(ns lein-vertx.repl
  (:require [vertx.repl :as vrepl]))

(defn init
  []
  (vrepl/start))
