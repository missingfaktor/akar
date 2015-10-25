(ns akar.special-operators
  (:require [akar.patterns.basic :refer [!var !pred]]
            [akar.internal.utilities :refer [append clump-after same-size?]]
            [akar.combinators :refer [!and]]))

(defn !at [!p]
  (!and !var !p))

(defn !guard [!p cond]
  (!and !p (!pred cond)))

; Pattern matching really shines when you have nested patterns. i.e. When Values extracted using
; one pattern can be further matched by other patterns. I am going to call this "furthering" a pattern.
;
; Let's define a combinator that allows us to "further" patterns.

(defn ^:private fan-out [root-extracts !nexts]
  (if (same-size? root-extracts !nexts)
    (let [pairings (map vector root-extracts !nexts)]
      (reduce
        (fn [extracts [in pattern]]
          (let [new-extracts (pattern in)]
            (if (nil? new-extracts)
              (reduced nil)
              (concat extracts new-extracts))))
        []
        pairings))))

(defn !further [!root & !nexts]
  (fn [arg]
    (if-let [root-extracts (!root arg)]
      (fan-out root-extracts !nexts))))

(defn !further-many
  ([!root !nexts] (fn [arg]
                    (if-let [[root-extracts] (!root arg)]
                      (fan-out root-extracts !nexts))))
  ([!root !nexts !rest] (fn [arg]
                          (if-let [[root-extracts] (!root arg)]
                            (let [root-extracts' (clump-after (count !nexts) root-extracts)
                                  !nexts' (append !nexts !rest)]
                              (fan-out root-extracts' !nexts'))))))
