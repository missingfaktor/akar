(ns akar.syntax
  (:require [clojure.spec.alpha :as s]
            [panini.core :refer [define-rule define-syntax]]
            [akar.primitives :refer [clause* clauses* match* or-else try-match*]]
            [akar.combinators :refer [!and !further !further-many !guard !or !view]]
            [akar.internal.utilities :refer [append]]
            [akar-commons.miscellaneous :refer [fail-with]]
            [akar.patterns :refer [!any !bind !constant !key !look-in !pred !record !seq !type !variant]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Validation functions

(defn ^:private ensuring-well-formed-bindings [bindings]
  (if (or (empty? bindings) (apply distinct? bindings))
    (vec bindings)
    (fail-with (str "Duplicate bindings encountered: " (vec bindings)))))

(defn ^:private ensuring-no-bindings-for-or [bindings]
  (if (empty? bindings)
    []
    (fail-with (str "Bindings encountered: " (vec bindings) \newline
                    ":or syntactic patterns do not support bindings." \newline
                    "Please ignore the bindings using :_"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Basic pattern rules

(define-rule any'
  :grammar #{:_ :any})

(define-rule literal'
  :grammar (s/or :number  number?
                 :string  string?
                 :boolean boolean?
                 :keyword keyword?
                 :nil     nil?))

(define-rule bind'
  :grammar (s/and symbol? #(not= % '&)))

(define-rule constant'
  :grammar (s/and list? (s/spec (s/cat :_tag #{:constant} :expr any?))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Combinator / composite pattern rules
;;; These reference ::pattern' which is registered below; spec resolves lazily.

(define-rule guard-pattern'
  :grammar (s/and list? (s/spec (s/cat :_tag  #{:guard}
                                       :inner ::pattern'
                                       :cond  any?))))

(define-rule view-pattern'
  :grammar (s/and list? (s/spec (s/cat :_tag    #{:view}
                                       :view-fn any?
                                       :inner   ::pattern'))))

(define-rule or-pattern'
  :grammar (s/and list? (s/spec (s/cat :_tag     #{:or}
                                       :patterns (s/+ ::pattern')))))

(define-rule and-pattern'
  :grammar (s/and list? (s/spec (s/cat :_tag     #{:and}
                                       :patterns (s/+ ::pattern')))))

(define-rule seq-pattern'
  :grammar (s/and list?
                  (s/spec (s/cat :_tag    #{:seq}
                                 :content (s/and vector?
                                               (s/spec (s/cat :elements (s/* ::pattern')
                                                              :rest     (s/? (s/cat :amp          #{'&}
                                                                                   :rest-pattern ::pattern')))))))))

(define-rule map-pattern'
  :grammar (s/and map? (s/map-of keyword? ::pattern')))

(define-rule look-in-pattern'
  :grammar (s/and list? (s/spec (s/cat :_tag     #{:look-in}
                                       :map-form any?
                                       :inner    ::pattern'))))

(define-rule variant-pattern'
  :grammar (s/and list?
                  (s/spec (s/cat :_tag   #{:variant}
                                 :tag    any?
                                 :fields (s/and vector? (s/spec (s/* ::pattern')))))))

(define-rule record-pattern'
  :grammar (s/and list?
                  (s/spec (s/cat :_tag   #{:record}
                                 :cls    any?
                                 :fields (s/and vector? (s/spec (s/* ::pattern')))))))

(define-rule type-pattern'
  :grammar (s/and list? (s/spec (s/cat :_tag #{:type} :cls any?))))

(define-rule arbitrary-pattern'
  :grammar (s/and vector? (s/spec (s/cat :combinator any?
                                         :patterns   (s/* ::pattern')))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Main pattern rule

(define-rule pattern'
  :grammar (s/or :any       ::any'
                 :literal   ::literal'
                 :constant  ::constant'
                 :bind      ::bind'
                 :guard     ::guard-pattern'
                 :view      ::view-pattern'
                 :or        ::or-pattern'
                 :and       ::and-pattern'
                 :seq       ::seq-pattern'
                 :map       ::map-pattern'
                 :look-in   ::look-in-pattern'
                 :variant   ::variant-pattern'
                 :record    ::record-pattern'
                 :type      ::type-pattern'
                 :arbitrary ::arbitrary-pattern'))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Pattern compilation
;;;
;;; Takes a conformed pattern' value (a tagged s/or pair [:tag value]) and
;;; returns {:pattern <pattern-expr> :bindings [sym ...]}.

(declare compile-pattern)

(defn ^:private compile-pattern [[tag value]]
  (case tag
    :any
    {:pattern  `!any 
     :bindings []}

    :literal
    (let [[_type lit] value]
      {:pattern  `(!constant ~lit) 
       :bindings []})

    :constant
    {:pattern  `(!constant ~(:expr value)) 
     :bindings []}

    :bind
    {:pattern  `!bind 
     :bindings [value]}

    :guard
    (let [{:keys [inner cond]} value
          inner-compiled       (compile-pattern inner)]
      {:pattern  `(!guard ~(:pattern inner-compiled) ~cond)
       :bindings (ensuring-well-formed-bindings (:bindings inner-compiled))})

    :view
    (let [{:keys [view-fn inner]} value
          inner-compiled          (compile-pattern inner)]
      {:pattern  `(!view ~view-fn ~(:pattern inner-compiled))
       :bindings (ensuring-well-formed-bindings (:bindings inner-compiled))})

    :or
    (let [{:keys [patterns]} value
          compiled           (map compile-pattern patterns)]
      {:pattern  `(!or ~@(map :pattern compiled))
       :bindings (->> compiled (mapcat :bindings) ensuring-no-bindings-for-or)})

    :and
    (let [{:keys [patterns]} value
          compiled           (map compile-pattern patterns)]
      {:pattern  `(!and ~@(map :pattern compiled))
       :bindings (->> compiled (mapcat :bindings) ensuring-well-formed-bindings)})

    :seq
    (let [{:keys [elements rest]} (:content value)
          compiled-elements       (map compile-pattern elements)
          compiled-rest           (some-> rest :rest-pattern compile-pattern)]
      {:pattern  (if (nil? compiled-rest)
                   `(!further-many !seq [~@(map :pattern compiled-elements)])
                   `(!further-many !seq [~@(map :pattern compiled-elements)] ~(:pattern compiled-rest)))
       :bindings (->> (if (nil? compiled-rest)
                        compiled-elements
                        (append compiled-elements compiled-rest))
                      (mapcat :bindings)
                      ensuring-well-formed-bindings)})

    :map 
    (let [compiled-entries (map (fn [[k conformed-v]]
                                  {:key      k 
                                   :compiled (compile-pattern conformed-v)})
                                value)]
      {:pattern  `(!and (!pred map?)
                        ~@(map (fn [{:keys [key compiled]}]
                                 `(!further (!key ~key) [~(:pattern compiled)]))
                               compiled-entries))
       :bindings (->> compiled-entries
                      (mapcat #(:bindings (:compiled %)))
                      ensuring-well-formed-bindings)})

    :look-in
    (let [{:keys [map-form inner]} value
          inner-compiled           (compile-pattern inner)]
      {:pattern  `(!further (!look-in ~map-form) [~(:pattern inner-compiled)])
       :bindings (ensuring-well-formed-bindings (:bindings inner-compiled))})

    :variant
    (let [{:keys [tag fields]} value
          compiled-fields      (map compile-pattern fields)]
      {:pattern  `(!further (!variant ~tag) [~@(map :pattern compiled-fields)])
       :bindings (->> compiled-fields (mapcat :bindings) ensuring-well-formed-bindings)})

    :record
    (let [{:keys [cls fields]} value
          compiled-fields      (map compile-pattern fields)]
      {:pattern  `(!further (!record ~cls) [~@(map :pattern compiled-fields)])
       :bindings (->> compiled-fields (mapcat :bindings) ensuring-well-formed-bindings)})

    :type
    {:pattern  `(!type ~(:cls value)) 
     :bindings []}

    :arbitrary
    (let [{:keys [combinator patterns]} value
          compiled-patterns             (map compile-pattern patterns)]
      {:pattern  (if (empty? compiled-patterns)
                   combinator
                   `(!further ~combinator [~@(map :pattern compiled-patterns)]))
       :bindings (->> compiled-patterns (mapcat :bindings) ensuring-well-formed-bindings)})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Clause rule and helper

(define-rule clause'
  :grammar (s/cat :pattern ::pattern' :action any?))

(defn ^:private compile-clause [{conformed-pattern :pattern action :action}]
  (let [{:keys [pattern bindings]} (compile-pattern conformed-pattern)]
    `(clause* ~pattern (fn [~@bindings] ~action))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Macros

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(define-syntax clause
  :grammar (s/cat :pattern ::pattern' :action any?)
  :target compile-clause)

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(define-syntax clauses
  :grammar (s/+ ::clause')
  :target (fn [clause-list]
            `(or-else ~@(map compile-clause clause-list))))

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(define-syntax match
  :grammar (s/cat :arg any? :clauses (s/+ ::clause'))
  :target (fn [{:keys [arg clauses]}]
            `(match* ~arg (or-else ~@(map compile-clause clauses)))))

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(define-syntax try-match
  :grammar (s/cat :arg any? :clauses (s/+ ::clause'))
  :target (fn [{:keys [arg clauses]}]
            `(try-match* ~arg (or-else ~@(map compile-clause clauses)))))

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(define-syntax if-match
  :grammar (s/cat :binding (s/and vector? (s/spec (s/cat :pattern ::pattern' :value any?)))
                  :then any?
                  :else (s/? any?))
  :target (fn [{:keys [binding then else]}]
            (let [{conformed-pattern :pattern 
                   value             :value}   binding
                  {:keys [pattern bindings]}   (compile-pattern conformed-pattern)]
              (if else
                `(match* ~value
                         (clauses* ~pattern (fn [~@bindings] ~then)
                                   !any (fn [] ~else)))
                `(match* ~value
                         (clauses* ~pattern (fn [~@bindings] ~then)
                                   !any (fn [] nil)))))))

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(define-syntax when-match
  :grammar (s/cat :binding (s/and vector? (s/spec (s/cat :pattern ::pattern' :value any?)))
                  :body (s/* any?))
  :target (fn [{:keys [binding body]}]
            (let [{conformed-pattern :pattern 
                   value             :value}   binding
                  {:keys [pattern bindings]}   (compile-pattern conformed-pattern)]
              `(match* ~value
                       (clauses* ~pattern (fn [~@bindings] (do ~@body))
                                 !any (fn [] nil))))))
