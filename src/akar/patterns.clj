(ns akar.patterns)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Basic patterns

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

(def !nil
  (!cst nil))

(def !true
  (!cst true))

(def !false
  (!cst false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Collection patterns

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; String patterns

(defn !regex [rgx]
  (fn [arg]
    (if (string? arg)
      (if-let [out (some->> arg
                            (re-seq rgx)
                            first)]
        (cond
          (string? out) []
          (sequential? out) [(rest out)])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; "Type" introspection patterns

(defn !type [type']
  (!pred (fn [arg]
           (= (type arg) type'))))

(defn !tag [tag]
  (!pred (fn [arg]
           (= (:tag arg) tag))))

(defn !class [class']
  (!pred (fn [arg]
           (instance? class' arg))))
