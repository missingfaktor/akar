(ns akar.syntax
  (:require [n01se.syntax :as sy]
            [n01se.seqex :refer [cap recap]]
            [akar.primitives :refer :all]
            [akar.combinators :refer :all]
            [akar.special-operators :refer :all]
            [akar.patterns :refer :all]))

(sy/defrule any-rule
            (cap (sy/alt :_ :any)
                 (fn [_]
                   {:pattern  `!any
                    :bindings []})))

(sy/defterminal number-literal number?)
(sy/defterminal string-literal string?)
(sy/defterminal boolean-literal (partial instance? Boolean))
(sy/defterminal keyword-literal keyword?)
(sy/defterminal nil-literal nil?)

(sy/defrule literal
            (cap (sy/alt number-literal
                         string-literal
                         boolean-literal
                         keyword-literal
                         nil-literal)
                 (fn [[lit]]
                   {:pattern  `(!cst ~lit)
                    :bindings []})))

(sy/defterminal binding-rule
                (cap symbol?
                     (fn [[sym]]
                       {:pattern  `!var
                        :bindings [sym]})))

(declare pattern-rule)

(sy/defrule arbitrary-pattern
            (recap (sy/vec-form (sy/cat (cap sy/form)
                                        (sy/rep* (delay pattern-rule))))
                   (fn [[pat] & pats]
                     {:pattern  (if (empty? pats)
                                  pat
                                  `(!further ~pat [~@(map :pattern pats)]))
                      :bindings (vec (mapcat :bindings pats))})))

(sy/defterminal map-key keyword?)

(sy/defrule map-entry
            (recap (sy/map-pair (cap map-key)
                                (delay pattern-rule))
                   (fn [[k] pat-result]
                     {:pattern  `(!further (!key ~k) [~(:pattern pat-result)])
                      :bindings (:bindings pat-result)})))

(sy/defrule map-pattern
            (recap (sy/map-form (sy/rep* map-entry))
                   (fn [& pat-results]
                     {:pattern  `(!and (!pred map?)
                                       ~@(map :pattern pat-results))
                      :bindings (vec (mapcat :bindings pat-results))})))

(sy/defrule at-pattern
            (recap (sy/list-form (sy/cat :as
                                         (cap sy/sym)
                                         (delay pattern-rule)))
                   (fn [[at-binding] inner-pat-results]
                     {:pattern  `(!at ~(:pattern inner-pat-results))
                      :bindings (vec (concat [at-binding]
                                             (:bindings inner-pat-results)))})))

(sy/defrule simple-pattern-rule
            (sy/alt any-rule
                    literal
                    binding-rule))

(sy/defrule complex-pattern-rule
            (sy/alt map-pattern
                    at-pattern
                    arbitrary-pattern))

(sy/defrule pattern-rule
            (sy/alt simple-pattern-rule
                    complex-pattern-rule))

(sy/defrule clause-rule
            (recap (sy/cat pattern-rule (cap sy/form))
                   (fn [{:keys [pattern bindings]} [action]]
                     `(clause* ~pattern (fn [~@bindings]
                                          ~action)))))

(sy/defrule clauses-rule
            (recap (sy/rep+ clause-rule)
                   (fn [& clss]
                     `(or-else ~@clss))))

(sy/defrule match-rule
            (recap (sy/cat (cap sy/form) clauses-rule)
                   (fn [[arg] clss]
                     `(match* ~arg ~clss))))

(sy/defrule try-match-rule
            (recap (sy/cat (cap sy/form) clauses-rule)
                   (fn [[arg] clss]
                     `(try-match* ~arg ~clss))))

; Macros
(sy/defsyntax clause clause-rule)
(sy/defsyntax clauses clauses-rule)
(sy/defsyntax match match-rule)
(sy/defsyntax try-match try-match-rule)
