(ns akar.special-operators
  (:require [akar.patterns.basic :refer [!var !pred]]
            [akar.combinators :refer [!and]]))

(defn !at [!p]
  (!and !var !p))

(defn !guard [!p cond]
  (!and !p (!pred cond)))

; Pattern matching really shines when you have nested patterns. i.e. When Values extracted using
; one pattern can be further matched by other patterns. I am going to call this "furthering" a pattern.
;
; Let's define a combinator that allows us to "further" patterns.

(defn ^:private fan-out [root-extracts !further-patterns]
  (let [pairings (map vector root-extracts !further-patterns)]
    (reduce
      (fn [extracts [in pattern]]
        (let [new-extracts (pattern in)]
          (if (nil? new-extracts)
            (reduced nil)
            (concat extracts new-extracts))))
      []
      pairings)))

(defn !further [!root-pattern & !further-patterns]
  (fn [arg]
    (if-let [root-extracts (!root-pattern arg)]
      (fan-out root-extracts !further-patterns))))

(defn !further-many [!root-pattern !further-patterns]
  (fn [arg]
    (if-let [[root-extracts] (!root-pattern arg)]
      (fan-out root-extracts !further-patterns))))
