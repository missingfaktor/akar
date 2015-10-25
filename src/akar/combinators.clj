(ns akar.combinators
  (:require [akar.patterns.basic :refer [!fail !var !pred]]
            [akar.internal.utilities :refer [variadic-reducive-function]]))

(def !and
  (variadic-reducive-function
    :zero !fail
    :combine (fn [!p1 !p2]
               (fn [arg]
                 (if-let [matches1 (!p1 arg)]
                   (if-let [matches2 (!p2 arg)]
                     (concat matches1 matches2)))))))

(defn !at [!p]
  (!and !var !p))

(defn !guard [!p cond]
  (!and !p (!pred cond)))

(def !or
  (variadic-reducive-function
    :zero !fail
    :combine (fn [!p1 !p2]
               (fn [arg]
                 (or (!p1 arg)
                     (!p2 arg))))))

(defn !not [!p]
  (fn [arg]
    (if (nil? (!p arg))
      []
      nil)))
