(ns akar.internal.utilities)

(defn variadic-reducive-function [& {:keys [zero combine]}]
  (fn
    ([] zero)
    ([f] f)
    ([f g] (combine f g))
    ([f g & more] (reduce combine (apply vector f g more)))))
