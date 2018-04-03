(ns akar-exceptions.internal.syntax-utilities
  (:require [clojure.spec.alpha :as sp]))

(defn define-syntax* [name' forms spec codegen]
  (let [enclosing-form (cons name' forms)
        syntax-tree (sp/conform spec enclosing-form)]
    (if (= ::sp/invalid syntax-tree)
      (throw (ex-info "Syntax error" (sp/explain-data spec enclosing-form)))
      (codegen syntax-tree))))

(defmacro define-syntax [name & {:keys [parser codegen]}]
  `(do
     (sp/def ~(:name parser) ~(:spec parser))
     (defmacro ~name [& forms#]
       (define-syntax* '~name forms# ~(:name parser) ~codegen))))
