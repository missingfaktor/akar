(ns akar.patterns.basic)

(def !any
  (fn [_]
    []))

(def !pfail
  (fn [_]
    nil))

(def !var
  (fn [arg]
    [arg]))

(defn !pred [pred]
  (fn [x]
    (when (pred x)
      [])))

(defn !cst [value]
  (!pred (fn [arg]
           (= value arg))))

(defn !view [f]
  (fn [arg]
    [(f arg)]))

(def !true
  (!cst true))

(def !false
  (!cst false))

(def !nil
  (!cst nil))
