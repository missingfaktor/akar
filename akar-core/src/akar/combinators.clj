(ns akar.combinators
  (:require [akar.patterns :refer [!fail !bind !pred !any]]
            [akar.internal.utilities :refer [append clump-after same-size?]]
            [akar-commons.miscellaneous :refer [single variadic-reductive-function]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Combinators to compose a number of patterns into one

(def ^{:doc "Combines patterns conjunctively. All component patterns must match,
and their emissions are concatenated in order."} !and
  (variadic-reductive-function
    :zero !any
    :combine (fn [!p1 !p2]
               (fn [arg]
                 (when-some [matches1 (!p1 arg)]
                   (when-some [matches2 (!p2 arg)]
                     (concat matches1 matches2)))))))

(def ^{:doc "Combines patterns disjunctively. Returns the first successful match."} !or
  (variadic-reductive-function
    :zero !fail
    :combine (fn [!p1 !p2]
               (fn [arg]
                 (or (!p1 arg)
                     (!p2 arg))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Combinators corresponding to common pattern operations

(defn !not
  "Matches when `!p` fails and emits nothing."
  [!p]
  (fn [arg]
    (if (nil? (!p arg))
      []
      nil)))

(defn !at
  "Matches with `!p` while also emitting the original input value."
  [!p]
  (!and !bind !p))

(defn !guard
  "Matches with `!p` and then requires `cond` to hold for the original input."
  [!p cond]
  (!and !p (!pred cond)))

(defn !view
  "Applies `f` to the input before matching it with `!p`."
  [f !p]
  (comp !p f))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; "Further" combinators

; To support nested patterns, we must allow values emitted by one pattern to be further
; matched by other patterns. What follows are a set of combinators that support such
; "furthering", and related features.

(defn ^:private fan-out [& {:keys [!root !nexts modify-root-emissions modify-nexts]}]
  (fn [arg]
    (when-some [root-emissions (!root arg)]
      (let [root-emissions' (modify-root-emissions root-emissions)
            !nexts'         (modify-nexts !nexts)]
        (when (same-size? root-emissions' !nexts')
          (let [pairings (map vector root-emissions' !nexts')]
            (reduce
             (fn [emissions [in pattern]]
               (let [new-emissions (pattern in)]
                 (if (nil? new-emissions)
                   (reduced nil)
                   (concat emissions new-emissions))))
             []
             pairings)))))))

(defn !further
  "Matches with `!root`, then matches each emitted value against the
  corresponding pattern in `!nexts`. Emits the concatenated emissions of
  those nested matches."
  [!root !nexts]
  (fan-out :!root !root
           :!nexts !nexts
           :modify-root-emissions identity
           :modify-nexts identity))

(defn !further-many
  "Like `!further`, but treats `!root` as emitting a single collection of
  values to be matched variadically. With three arguments, `!rest` matches the
  remaining emitted values after `!nexts` are satisfied."
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

; Aliases for succinctness in direct use
(def ^{:doc "Alias for `!further`."
       :alias-for #'!further} !f !further)
(def ^{:doc "Alias for `!further-many`."
       :alias-for #'!further-many} !f* !further-many)
