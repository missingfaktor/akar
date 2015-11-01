(ns akar.special-operators
  (:require [akar.patterns :refer [!var !pred]]
            [akar.internal.utilities :refer [append clump-after same-size? single]]
            [akar.combinators :refer [!and]]))

(defn !at [!p]
  (!and !var !p))

(defn !guard [!p cond]
  (!and !p (!pred cond)))

; To support nested patterns, we must allow values emitted by one pattern to be further
; matched by other patterns. What follows are a set of combinators that support such
; "furthering", and related features.

(defn ^:private fan-out [& {:keys [!root !nexts modify-root-emissions modify-nexts]}]
  (fn [arg]
    (if-let [root-emissions (!root arg)]
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
