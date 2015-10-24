(ns akar.patterns.collection
  (:require [akar.patterns.basic :refer [!pred]]
            [akar.combinators :refer [!furthering]]))

(def !empty
  (!pred empty?))

(def !cons
  (fn [arg]
    (when (not-empty arg)
      [(first arg) (rest arg)])))

(defn !key [key]
  (fn [map']
    (if-let [value (map' key)]
      [value])))
