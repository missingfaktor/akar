(ns akar.patterns.collection
  (:require [akar.patterns.basic :refer [!pred]]
            [akar.combinators :refer [!furthering]]))

(def !empty
  (!pred (fn [arg]
           (and (sequential? arg)
                (empty? arg)))))

(def !cons
  (fn [arg]
    (if (and (sequential? arg) (not-empty arg))
      [(first arg) (rest arg)])))

(defn !key [key]
  (fn [map']
    (if (map? map')
      (if-let [value (map' key)]
        [value]))))

(defn !optional-key [key]
  (fn [map']
    (if (map? map')
      [(map' key)])))
