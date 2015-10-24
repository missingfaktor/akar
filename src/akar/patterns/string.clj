(ns akar.patterns.string)

(defn !regex [rgx]
  (fn [arg]
    (if (string? arg)
      (if-let [captures (re-seq rgx arg)]
        [(->> captures first rest)]))))
