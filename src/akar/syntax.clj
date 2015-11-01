(ns akar.syntax
  (:require [n01se.syntax :as sy]
            [n01se.seqex :refer [cap recap]]
            [akar.primitives :refer :all]
            [akar.combinators :refer :all]
            [akar.internal.utilities :refer :all]
            [akar.patterns :refer :all]))

(sy/defrule any'
            (cap (sy/alt :_ :any)
                 (fn [_]
                   {:pattern  `!any
                    :bindings []})))

(sy/defterminal number-literal' number?)
(sy/defterminal string-literal' string?)
(sy/defterminal boolean-literal' (partial instance? Boolean))
(sy/defterminal keyword-literal' keyword?)
(sy/defterminal nil-literal' nil?)

(sy/defterminal valid-symbol' (fn [sym]
                                (and (symbol? sym)
                                     (not= sym '&))))

(sy/defrule literal'
            (cap (sy/alt number-literal'
                         string-literal'
                         boolean-literal'
                         keyword-literal'
                         nil-literal')
                 (fn [[lit]]
                   {:pattern  `(!cst ~lit)
                    :bindings []})))

(sy/defterminal binding'
                (cap valid-symbol'
                     (fn [[sym]]
                       {:pattern  `!var
                        :bindings [sym]})))

(declare pattern')

(sy/defrule arbitrary-pattern'
            (recap (sy/vec-form (sy/cat (cap sy/form)
                                        (sy/rep* (delay pattern'))))
                   (fn [[pat] & pats]
                     {:pattern  (if (empty? pats)
                                  pat
                                  `(!further ~pat [~@(map :pattern pats)]))
                      :bindings (vec (mapcat :bindings pats))})))

(sy/defterminal map-key' keyword?)

(sy/defrule map-entry'
            (recap (sy/map-pair (cap map-key')
                                (delay pattern'))
                   (fn [[k] pat-result]
                     {:pattern  `(!further (!key ~k) [~(:pattern pat-result)])
                      :bindings (:bindings pat-result)})))

(sy/defrule map-pattern'
            (recap (sy/map-form (sy/rep* map-entry'))
                   (fn [& pat-results]
                     {:pattern  `(!and (!pred map?)
                                       ~@(map :pattern pat-results))
                      :bindings (vec (mapcat :bindings pat-results))})))

(sy/defrule seq-pattern'
            (recap (sy/list-form (sy/cat :seq
                                         (sy/vec-form (sy/cat (recap (sy/rep* (delay pattern'))
                                                                     (fn [& pats] {:patterns pats}))
                                                              (sy/opt (recap (sy/cat '& (delay pattern'))
                                                                             (fn [rest] {:rest rest})))))))
                   (fn [& captures]
                     (let [captures-map (apply merge captures)
                           patterns (:patterns captures-map)
                           rest (:rest captures-map)]
                       {:pattern  (if (nil? rest)
                                    `(!further-many !seq [~@(map :pattern patterns)])
                                    `(!further-many !seq [~@(map :pattern patterns)] ~(:pattern rest)))
                        :bindings (if (nil? rest)
                                    (vec (mapcat :bindings patterns))
                                    (vec (mapcat :bindings (append patterns rest))))}))))

(sy/defrule at-pattern'
            (recap (sy/list-form (sy/cat :as
                                         (cap valid-symbol')
                                         (delay pattern')))
                   (fn [[at-binding] inner-pat-results]
                     {:pattern  `(!at ~(:pattern inner-pat-results))
                      :bindings (vec (concat [at-binding]
                                             (:bindings inner-pat-results)))})))

(sy/defrule guard-pattern'
            (recap (sy/list-form (sy/cat :guard
                                         (delay pattern')
                                         (cap sy/form)))
                   (fn [inner-pat-result [cond]]
                     {:pattern  `(!guard ~(:pattern inner-pat-result) ~cond)
                      :bindings (:bindings inner-pat-result)})))

; https://ghc.haskell.org/trac/ghc/wiki/ViewPatterns
(sy/defrule view-pattern'
            (recap (sy/list-form (sy/cat :view
                                         (cap sy/form)
                                         (delay pattern')))
                   (fn [[view-fn] pat]
                     {:pattern  `(!further (!view ~view-fn) [~(:pattern pat)])
                      :bindings (:bindings pat)})))

(sy/defrule simple-pattern'
            (sy/alt any'
                    literal'
                    binding'))

(sy/defrule complex-pattern'
            (sy/alt seq-pattern'
                    map-pattern'
                    guard-pattern'
                    at-pattern'
                    view-pattern'
                    arbitrary-pattern'))

(sy/defrule pattern'
            (sy/alt simple-pattern'
                    complex-pattern'))

(sy/defrule clause'
            (recap (sy/cat pattern' (cap sy/form))
                   (fn [{:keys [pattern bindings]} [action]]
                     `(clause* ~pattern (fn [~@bindings]
                                          ~action)))))

(sy/defrule clauses'
            (recap (sy/rep+ clause')
                   (fn [& clss]
                     `(or-else ~@clss))))

(sy/defrule match'
            (recap (sy/cat (cap sy/form) clauses')
                   (fn [[arg] clss]
                     `(match* ~arg ~clss))))

(sy/defrule try-match'
            (recap (sy/cat (cap sy/form) clauses')
                   (fn [[arg] clss]
                     `(try-match* ~arg ~clss))))

; Macros
(sy/defsyntax clause clause')
(sy/defsyntax clauses clauses')
(sy/defsyntax match match')
(sy/defsyntax try-match try-match')
