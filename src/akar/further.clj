(ns akar.further)

; Pattern matching really shines when you have nested patterns. i.e. When Values extracted using
; one pattern can be further matched by other patterns. I am going to call this "furthering" a pattern.
;
; Let's define a combinator that allows us to "further" patterns.

(defn !further [!root-pattern & !further-patterns]
  (fn [arg]
    (if-let [root-extracts (!root-pattern arg)]
      (let [pairings (map vector root-extracts !further-patterns)]
        (reduce
          (fn [extracts [in pattern]]
            (let [new-extracts (pattern in)]
              (if (nil? new-extracts)
                (reduced nil)
                (concat extracts new-extracts))))
          []
          pairings)))))
