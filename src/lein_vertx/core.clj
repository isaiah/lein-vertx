(ns lein-vertx.core
  (:require [clojure.string :as s]))

(defn verticlize [x]
  (s/join (map s/capitalize (s/split x #"\."))))
