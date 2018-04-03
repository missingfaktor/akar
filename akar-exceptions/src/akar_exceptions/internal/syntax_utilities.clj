(ns akar-exceptions.internal.syntax-utilities
  (:require [clojure.spec.alpha :as sp]))

(defn define-syntax* [name' forms spec target]
  (let [enclosing-form (cons name' forms)
        result (sp/conform spec enclosing-form)]
    (if (= ::sp/invalid result)
      (throw (ex-info "Syntax error" (sp/explain-data spec enclosing-form)))
      (target result))))

(defmacro define-syntax [name & {:keys [spec-name spec target]}]
  `(do
     (sp/def ~spec-name ~spec)
     (defmacro ~name [& forms#]
       (define-syntax* '~name forms# ~spec-name ~target))))
