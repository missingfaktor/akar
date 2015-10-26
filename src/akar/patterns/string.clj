(ns akar.patterns.string)

(defn !regex [rgx]
  (fn [arg]
    (if (string? arg)
      (if-let [out (some->> arg
                            (re-seq rgx)
                            first)]
        (cond
          (string? out) []
          (sequential? out) [(rest out)])))))
