(ns akar.syntax
  (:require [n01se.syntax :as sy]
            [n01se.seqex :refer [cap recap]]
            [akar.primitives :refer :all]
            [akar.patterns.basic :refer :all]))

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

(sy/defterminal arbitrary-pattern
                (recap (sy/vec-form (cap sy/form))
                       (fn [[pat]]
                         {:pattern  pat
                          :bindings []})))

(sy/defrule simple-pattern-rule
            (sy/alt any-rule
                    literal
                    binding-rule))

(sy/defrule complex-pattern-rule
            (sy/alt arbitrary-pattern))

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
