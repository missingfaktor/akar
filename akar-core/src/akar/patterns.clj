(ns akar.patterns)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Basic patterns

(def ^{:doc "Matches any value and emits nothing."} !any
  (fn [_]
    []))

(def ^{:doc "Matches no value."} !fail
  (fn [_]
    nil))

(def ^{:doc "Matches any value and emits that value."} !bind
  (fn [arg]
    [arg]))

(defn !pred
  "Builds a pattern from a predicate. Matches when `pred` returns truthy
  for the input and emits nothing."
  [pred]
  (fn [x]
    (when (pred x)
      [])))

(defn !constant
  "Matches values equal to `value` and emits nothing."
  [value]
  (!pred (fn [arg]
           (= value arg))))

; Matches any non-nil value, including `false`.
; Follows Clojure's `some?` semantics: present ≠ nil.
(def ^{:doc "Matches any non-nil value, including `false`, and emits nothing."} !some
  (!pred some?))

(def ^{:doc "Matches `nil` and emits nothing."} !nil
  (!constant nil))

(def ^{:doc "Matches `true` and emits nothing."} !true
  (!constant true))

(def ^{:doc "Matches `false` and emits nothing."} !false
  (!constant false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Collection patterns

(def ^{:doc "Matches an empty sequential collection and emits nothing."} !empty
  (!pred (fn [arg]
           (and (sequential? arg)
                (empty? arg)))))

(def ^{:doc "Matches a non-empty sequential collection and emits its head and tail."} !cons
  (fn [arg]
    (when (and (sequential? arg) (not-empty arg))
      [(first arg) (rest arg)])))

(def ^{:doc "Matches a sequential collection and emits it as a vector."} !seq
  (fn [arg]
    (when (sequential? arg)
      [(vec arg)])))

(defn !key
  "Matches maps that contain `key` and emits the corresponding value."
  [key]
  (fn [arg]
    (when (map? arg)
      (when (contains? arg key)
        [(get arg key)]))))

(defn !optional-key
  "Matches any map and emits the value at `key`, or `nil` when the key is absent."
  [key]
  (fn [arg]
    (when (map? arg)
      [(get arg key)])))

(defn !look-in
  "Builds a pattern from a lookup map. Matches when the input is present as a
  key in `map` and emits the corresponding value."
  [map]
  (fn [arg]
    (when-some [value (get map arg)]
      [value])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Data type patterns

; Variants, as described by Jeanine Adkisson here - https://www.youtube.com/watch?v=ZQkIWWTygio
(defn !variant
  "Matches a tagged vector whose first element is `tag` and emits the
  remaining elements."
  [tag]
  (fn [arg]
    (when (and (vector? arg) (= (first arg) tag))
      (vec (rest arg)))))

(defn !record
  "Matches records of class `cls` and emits their field values."
  [cls]
  (fn [arg]
    (when (and (record? arg) (instance? cls arg))
      (vec (vals arg)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; String patterns

(defn !regex
  "Matches strings against `rgx` and emits its capturing groups in order.
  A successful match with no capturing groups emits an empty vector."
  [rgx]
  (fn [arg]
    (when (string? arg)
      (let [matcher (re-matcher rgx arg)]
        (when (.find matcher)
          (mapv #(.group matcher %) (range 1 (inc (.groupCount matcher)))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Type-casing patterns

(defn !type
  "Matches values whose type equals `type-being-matched-against`. When both
  sides are classes, uses Java assignability instead of exact equality."
  [type-being-matched-against]
  (!pred (fn [arg]
           (let [type-of-arg (type arg)]
             (if (and (instance? Class type-of-arg)
                      (instance? Class type-being-matched-against))
               (.isAssignableFrom type-being-matched-against type-of-arg)
               (= type-of-arg type-being-matched-against))))))

(defn !tag
  "Matches maps whose `:tag` equals `tag` and emits nothing."
  [tag]
  (!pred (fn [arg]
           (= (:tag arg) tag))))
