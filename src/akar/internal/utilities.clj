(ns akar.internal.utilities)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; General utilities used elsewhere in the library. Not intended for public
;;; consumption.

(defn variadic-reductive-function [& {:keys [zero combine]}]
  (fn
    ([] zero)
    ([f] f)
    ([f g] (combine f g))
    ([f g & more] (reduce combine (apply vector f g more)))))

(defn append [coll x]
  (conj (vec coll) x))

(defn clump-after [n coll]
  (let [[xs ys] (split-at n coll)]
    (append xs (vec ys))))

(defn same-size? [xs ys]
  (= (count xs) (count ys)))

(defn single [coll]
  (let [[x xs] [(first coll) (next coll)]]
    (if (and x (not xs))
      x
      (throw (RuntimeException.
               (str "Collection does not contain a single element. The size is " (count coll) "."))))))

(defmacro define-alias [alias original]
  `(def ~(vary-meta alias assoc :alias-for original) ~original))
