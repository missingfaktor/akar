(ns akar.special-operators
  (:require [akar.patterns.basic :refer [!var !pred]]
            [akar.internal.utilities :refer [append clump-after same-size? single]]
            [akar.combinators :refer [!and]]))

(defn !at [!p]
  (!and !var !p))

(defn !guard [!p cond]
  (!and !p (!pred cond)))

; Pattern matching really shines when you have nested patterns. i.e. When Values extracted using
; one pattern can be further matched by other patterns. I am going to call this "furthering" a pattern.
;
; Let's define a combinator that allows us to "further" patterns.

(defn ^:private fan-out [& {:keys [!root !nexts modify-root-extracts modify-nexts]}]
  (fn [arg]
    (if-let [root-extracts (!root arg)]
      (let [root-extracts' (modify-root-extracts root-extracts)
            !nexts' (modify-nexts !nexts)]
        (if (same-size? root-extracts' !nexts')
          (let [pairings (map vector root-extracts' !nexts')]
            (reduce
              (fn [extracts [in pattern]]
                (let [new-extracts (pattern in)]
                  (if (nil? new-extracts)
                    (reduced nil)
                    (concat extracts new-extracts))))
              []
              pairings)))))))

(defn !further [!root !nexts]
  (fan-out :!root !root
           :!nexts !nexts
           :modify-root-extracts identity
           :modify-nexts identity))

(defn !further-many
  ([!root !nexts] (fan-out :!root !root
                           :!nexts !nexts
                           :modify-root-extracts single
                           :modify-nexts identity))
  ([!root !nexts !rest] (fan-out :!root !root
                                 :!nexts !nexts
                                 :modify-root-extracts (fn [root-extracts]
                                                         (->> root-extracts
                                                              single
                                                              (clump-after (count !nexts))))
                                 :modify-nexts (fn [!nexts]
                                                 (append !nexts !rest)))))
