(ns akar.syntax
  (:require [n01se.syntax :as sy]
            [n01se.seqex :refer [cap recap]]
            [akar.primitives :refer :all]
            [akar.patterns.basic :refer :all]))

(sy/defterminal NumberLiteral number?)
(sy/defterminal StringLiteral string?)
(sy/defterminal BooleanLiteral (partial instance? Boolean))
(sy/defterminal KeywordLiteral keyword?)
(sy/defterminal NilLiteral nil?)

(sy/defrule Literal
            (cap (sy/alt NumberLiteral
                         StringLiteral
                         BooleanLiteral
                         KeywordLiteral
                         NilLiteral)
                 (fn [[lit]]
                   `(!cst ~lit))))

(sy/defrule Any
            (cap (sy/alt :_ :any)
                 (constantly `!any)))

(sy/defrule Pattern
            (sy/alt Any
                    Literal))

(sy/defrule Clause
            (recap (sy/cat Pattern (cap sy/form))
                   (fn [pat [action]]
                     `(clause* ~pat (fn []
                                      ~action)))))

(sy/defrule Clauses
            (recap (sy/rep+ Clause)
                   (fn [& clss]
                     `(or-else ~@clss))))

(sy/defrule Match
            (recap (sy/cat (cap sy/form) Clauses)
                   (fn [[arg] clss]
                     `(match* ~arg ~clss))))

(sy/defrule TryMatch
            (recap (sy/cat (cap sy/form) Clauses)
                   (fn [[arg] clss]
                     `(try-match* ~arg ~clss))))

; Macros
(sy/defsyntax clause Clause)
(sy/defsyntax clauses Clauses)
(sy/defsyntax match Match)
(sy/defsyntax try-match TryMatch)
