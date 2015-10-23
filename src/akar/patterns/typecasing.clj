(ns akar.patterns.typecasing
  (:require [akar.patterns.basic :refer :all]))

(defn !type [type']
  (!pred (fn [arg]
           (= (type arg) type'))))

(defn !tag [tag]
  (!pred (fn [arg]
           (= (:tag arg) tag))))

(defn !class [class']
  (!pred (fn [arg]
           (instance? class' arg))))
