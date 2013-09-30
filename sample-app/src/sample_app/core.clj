(ns sample-app.core
  (:require [vertx.core :as vertx]))

(defn init
  []
  (vertx/periodic 1000
                  (println "I'm working!")))
