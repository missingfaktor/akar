(ns akar.patterns)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Basic patterns

(def !any
  (fn [_]
    []))

(def !fail
  (fn [_]
    nil))

(def !bind
  (fn [arg]
    [arg]))

(defn !pred [pred]
  (fn [x]
    (when (pred x)
      [])))

(defn !constant [value]
  (!pred (fn [arg]
           (= value arg))))

(def !some
  (!pred (comp not nil?)))

(def !nil
  (!constant nil))

(def !true
  (!constant true))

(def !false
  (!constant false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Collection patterns

(def !empty
  (!pred (fn [arg]
           (and (sequential? arg)
                (empty? arg)))))

(def !cons
  (fn [arg]
    (when (and (sequential? arg) (not-empty arg))
      [(first arg) (rest arg)])))

(def !seq
  (fn [arg]
    (when (sequential? arg)
      [(vec arg)])))

(defn !key [key]
  (fn [arg]
    (when (map? arg)
      (when-some [value (get arg key)]
        [value]))))

(defn !optional-key [key]
  (fn [arg]
    (when (map? arg)
      [(get arg key)])))

(defn !look-in [map]
  (fn [arg]
    (when-some [value (get map arg)]
      [value])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Data type patterns

; Variants, as described by Jeanine Adkisson here - https://www.youtube.com/watch?v=ZQkIWWTygio
(defn !variant [tag]
  (fn [arg]
    (when (and (vector? arg) (= (first arg) tag))
      (vec (rest arg)))))

(defn !record [cls]
  (fn [arg]
    (when (and (record? arg) (instance? cls arg))
      (vec (vals arg)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; String patterns

(defn !regex [rgx]
  (fn [arg]
    (when (string? arg)
      (when-some [out (some->> arg
                             (re-seq rgx)
                             first)]
        (cond
          (string? out) []
          (sequential? out) (vec (rest out)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Type-casing patterns

(defn !type [type-being-matched-against]
  (!pred (fn [arg]
           (let [type-of-arg (type arg)]
             (if (and (instance? Class type-of-arg)
                      (instance? Class type-being-matched-against))
               (.isAssignableFrom type-being-matched-against type-of-arg)
               (= type-of-arg type-being-matched-against))))))

(defn !tag [tag]
  (!pred (fn [arg]
           (= (:tag arg) tag))))
