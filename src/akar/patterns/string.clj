(ns akar.patterns.string)

(defn !regex [rgx]
  (fn [arg]
    (when-let [captures (re-seq rgx arg)]
      [(->> captures first rest)])))
