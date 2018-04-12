(ns akar.internal.utilities)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; General utilities used elsewhere in the library. Not intended for public
;;; consumption.

(defn append [coll x]
  (conj (vec coll) x))

(defn clump-after [n coll]
  (let [[xs ys] (split-at n coll)]
    (append xs (vec ys))))

(defn same-size? [xs ys]
  (= (count xs) (count ys)))
