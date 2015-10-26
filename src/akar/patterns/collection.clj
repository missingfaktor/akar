(ns akar.patterns.collection
  (:require [akar.patterns.basic :refer [!pred]]))

(def !empty
  (!pred (fn [arg]
           (and (sequential? arg)
                (empty? arg)))))

(def !cons
  (fn [arg]
    (if (and (sequential? arg) (not-empty arg))
      [(first arg) (rest arg)])))

(def !seq
  (fn [arg]
    (if (sequential? arg)
      [(vec arg)])))

(defn !key [key]
  (fn [arg]
    (if (map? arg)
      (if-let [value (arg key)]
        [value]))))

(defn !optional-key [key]
  (fn [arg]
    (if (map? arg)
      [(arg key)])))

; Variants, as described by Jeanine Adkisson here - https://www.youtube.com/watch?v=ZQkIWWTygio
(defn !variant [tag]
  (fn [arg]
    (if (and (vector? arg) (= (first arg) tag))
      (vec (rest arg)))))
