(ns akar.combinators
  (:use [akar.patterns.basic :refer [!fail !var]]
        [akar.internal.utilities :refer [variadic-reducive-function]]))

; Combining patterns.

(def !and
  (variadic-reducive-function
    :zero !fail
    :combine (fn [!p1 !p2]
               (fn [arg]
                 (when-let [matches1 (!p1 arg)]
                   (when-let [matches2 (!p2 arg)]
                     (concat matches1 matches2)))))))

(defn !at [!p]
  (!and !var !p))

(def !or
  (variadic-reducive-function
    :zero !fail
    :combine (fn [!p1 !p2]
               (fn [arg]
                 (or (!p1 arg)
                     (!p2 arg))))))

(defn !not [!p]
  (fn [arg]
    (if (nil? (!p arg))
      []
      nil)))

; Pattern matching really shines when you have nested patterns. i.e. When Values extracted using
; one pattern can be further matched by other patterns. I am going to call such patterns "furthering"
; patterns.
;
; Let's define a combinator that takes in a pattern, applies it, and if there are matches,
; passes them on to next set of patterns.

(defn !furthering [!root-pattern]
  (fn [& !further-patterns]
    (fn [arg]
      (when-let [root-extracts (!root-pattern arg)]
        (let [pairings (map vector root-extracts !further-patterns)]
          (reduce
            (fn [extracts [in pattern]]
              (let [new-extracts (pattern in)]
                (if (nil? new-extracts)
                  (reduced nil)
                  (concat extracts new-extracts))))
            []
            pairings))))))