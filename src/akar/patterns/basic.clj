(ns akar.patterns.basic)

(def !any
  (fn [_]
    []))

(def !fail
  (fn [_]
    nil))

(def !var
  (fn [arg]
    [arg]))

(defn !pred [pred]
  (fn [x]
    (if (pred x)
      [])))

(defn !cst [value]
  (!pred (fn [arg]
           (= value arg))))

(defn !view [f]
  (fn [arg]
    [(f arg)]))

(def !some
  (fn [arg]
    (if (nil? arg)
      nil
      [arg])))

(def !true
  (!cst true))

(def !false
  (!cst false))

(def !nil
  (!cst nil))
