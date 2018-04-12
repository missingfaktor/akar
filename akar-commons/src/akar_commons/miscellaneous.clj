(ns akar-commons.miscellaneous)

(defn variadic-reductive-function [& {:keys [zero combine]}]
  (fn
    ([] zero)
    ([f] f)
    ([f g] (combine f g))
    ([f g & more] (reduce combine (apply vector f g more)))))

(defn fail-with [msg]
  (throw (RuntimeException. ^String msg)))

(defn single [coll]
  (let [[x xs] [(first coll) (next coll)]]
    (if (and x (not xs))
      x
      (fail-with (str "Collection does not contain a single element. The size is " (count coll) ".")))))

(defmacro define-alias [alias original]
  `(def ~(vary-meta alias assoc :alias-for original) ~original))
