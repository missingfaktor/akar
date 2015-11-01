(ns akar.combinators
  (:require [akar.patterns :refer [!fail !var !pred]]
            [akar.internal.utilities :refer :all]))

(def !and
  (variadic-reductive-function
    :zero !fail
    :combine (fn [!p1 !p2]
               (fn [arg]
                 (if-some [matches1 (!p1 arg)]
                   (if-some [matches2 (!p2 arg)]
                     (concat matches1 matches2)))))))

(def !or
  (variadic-reductive-function
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

(defn !at [!p]
  (!and !var !p))

(defn !guard [!p cond]
  (!and !p (!pred cond)))

; To support nested patterns, we must allow values emitted by one pattern to be further
; matched by other patterns. What follows are a set of combinators that support such
; "furthering", and related features.

(defn ^:private fan-out [& {:keys [!root !nexts modify-root-emissions modify-nexts]}]
  (fn [arg]
    (if-some [root-emissions (!root arg)]
      (let [root-emissions' (modify-root-emissions root-emissions)
            !nexts' (modify-nexts !nexts)]
        (if (same-size? root-emissions' !nexts')
          (let [pairings (map vector root-emissions' !nexts')]
            (reduce
              (fn [emissions [in pattern]]
                (let [new-emissions (pattern in)]
                  (if (nil? new-emissions)
                    (reduced nil)
                    (concat emissions new-emissions))))
              []
              pairings)))))))

(defn !further [!root !nexts]
  (fan-out :!root !root
           :!nexts !nexts
           :modify-root-emissions identity
           :modify-nexts identity))

(defn !further-many
  ([!root !nexts] (fan-out :!root !root
                           :!nexts !nexts
                           :modify-root-emissions single
                           :modify-nexts identity))
  ([!root !nexts !rest] (fan-out :!root !root
                                 :!nexts !nexts
                                 :modify-root-emissions (fn [root-emissions]
                                                          (->> root-emissions
                                                               single
                                                               (clump-after (count !nexts))))
                                 :modify-nexts (fn [!nexts]
                                                 (append !nexts !rest)))))
