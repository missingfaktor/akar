(ns akar.syntax
  (:require [n01se.syntax :as sy]
            [n01se.seqex :refer [cap recap]]
            [akar.primitives :refer :all]
            [akar.combinators :refer :all]
            [akar.internal.utilities :refer :all]
            [akar.patterns :refer :all]))

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
;;; Basic patterns

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

(sy/defrule literal'
            (cap (sy/alt number-literal'
                         string-literal'
                         boolean-literal'
                         keyword-literal'
                         nil-literal')
                 (fn [[lit]]
                   {:pattern  `(!constant ~lit)
                    :bindings []})))

(sy/defterminal valid-symbol' (fn [sym]
                                (and (symbol? sym)
                                     (not= sym '&))))

(sy/defrule constant'
            (recap (sy/list-form (sy/cat :constant (cap sy/form)))
                   (fn [[expression]]
                     {:pattern  `(!constant ~expression)
                      :bindings []})))

(sy/defrule bind'
            (cap valid-symbol'
                 (fn [[sym]]
                   {:pattern  `!bind
                    :bindings [sym]})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Combinators / special pattern matching features

(declare pattern')

(sy/defrule guard-pattern'
            (recap (sy/list-form (sy/cat :guard
                                         (delay pattern')
                                         (cap sy/form)))
                   (fn [inner-syntactic-pattern [cond]]
                     {:pattern  `(!guard ~(:pattern inner-syntactic-pattern) ~cond)
                      :bindings (->> inner-syntactic-pattern
                                     :bindings
                                     ensuring-well-formed-bindings)})))

; https://ghc.haskell.org/trac/ghc/wiki/ViewPatterns
(sy/defrule view-pattern'
            (recap (sy/list-form (sy/cat :view
                                         (cap sy/form)
                                         (delay pattern')))
                   (fn [[view-fn] syntactic-pattern]
                     {:pattern  `(!view ~view-fn ~(:pattern syntactic-pattern))
                      :bindings (->> syntactic-pattern
                                     :bindings
                                     ensuring-well-formed-bindings)})))

(sy/defrule or-pattern'
            (recap (sy/list-form (sy/cat :or
                                         (sy/rep+ (delay pattern'))))
                   (fn [& syntactic-patterns]
                     {:pattern  `(!or ~@(map :pattern syntactic-patterns))
                      :bindings (->> syntactic-patterns
                                     (mapcat :bindings)
                                     ensuring-no-bindings-for-or)})))

(sy/defrule and-pattern'
            (recap (sy/list-form (sy/cat :and
                                         (sy/rep+ (delay pattern'))))
                   (fn [& syntactic-patterns]
                     {:pattern  `(!and ~@(map :pattern syntactic-patterns))
                      :bindings (->> syntactic-patterns
                                     (mapcat :bindings)
                                     ensuring-well-formed-bindings)})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Data type patterns

(sy/defrule seq-pattern'
            (recap (sy/list-form (sy/cat :seq
                                         (sy/vec-form (sy/cat (recap (sy/rep* (delay pattern'))
                                                                     (fn [& synactic-patterns]
                                                                       {:patterns synactic-patterns}))
                                                              (sy/opt (recap (sy/cat '& (delay pattern'))
                                                                             (fn [rest-synactic-pattern]
                                                                               {:rest rest-synactic-pattern})))))))
                   (fn [& captures]
                     (let [captures-map (apply merge captures)
                           patterns (:patterns captures-map)
                           rest (:rest captures-map)]
                       {:pattern  (if (nil? rest)
                                    `(!further-many !seq [~@(map :pattern patterns)])
                                    `(!further-many !seq [~@(map :pattern patterns)] ~(:pattern rest)))
                        :bindings (->> (if (nil? rest)
                                         patterns
                                         (append patterns rest))
                                       (mapcat :bindings)
                                       ensuring-well-formed-bindings)}))))

(sy/defterminal map-key' keyword?)

(sy/defrule map-entry'
            (recap (sy/map-pair (cap map-key')
                                (delay pattern'))
                   (fn [[k] syntactic-pattern]
                     {:pattern  `(!further (!key ~k) [~(:pattern syntactic-pattern)])
                      :bindings (->> syntactic-pattern
                                     :bindings
                                     ensuring-well-formed-bindings)})))

(sy/defrule map-pattern'
            (recap (sy/map-form (sy/rep* map-entry'))
                   (fn [& syntactic-patterns]
                     {:pattern  `(!and (!pred map?)
                                       ~@(map :pattern syntactic-patterns))
                      :bindings (->> syntactic-patterns
                                     (mapcat :bindings)
                                     ensuring-well-formed-bindings)})))

(sy/defrule look-in-pattern'
            (recap (sy/list-form (sy/cat :look-in (cap sy/form) (delay pattern')))
                   (fn [[map] syntactic-pattern]
                     {:pattern  `(!further (!look-in ~map) [~(:pattern syntactic-pattern)])
                      :bindings (->> syntactic-pattern
                                     :bindings
                                     ensuring-well-formed-bindings)})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Data type patterns

(sy/defrule variant-pattern'
            (recap (sy/list-form (sy/cat :variant
                                         (cap sy/form)
                                         (sy/vec-form (sy/rep* (delay pattern')))))
                   (fn [[tag] & syntactic-patterns]
                     {:pattern  `(!further (!variant ~tag) [~@(map :pattern syntactic-patterns)])
                      :bindings (->> syntactic-patterns
                                     (mapcat :bindings)
                                     ensuring-well-formed-bindings)})))

(sy/defrule record-pattern'
            (recap (sy/list-form (sy/cat :record
                                         (cap sy/form)
                                         (sy/vec-form (sy/rep* (delay pattern')))))
                   (fn [[cls] & syntactic-patterns]
                     {:pattern  `(!further (!record ~cls) [~@(map :pattern syntactic-patterns)])
                      :bindings (->> syntactic-patterns
                                     (mapcat :bindings)
                                     ensuring-well-formed-bindings)})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Type-casing pattern

(sy/defrule type-pattern'
            (recap (sy/list-form (sy/cat :type
                                         (cap sy/form)))
                   (fn [[cls]]
                     {:pattern  `(!type ~cls)
                      :bindings []})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Arbitrary patterns

(sy/defrule arbitrary-pattern'
            (recap (sy/vec-form (sy/cat (cap sy/form)
                                        (sy/rep* (delay pattern'))))
                   (fn [[syntactic-pattern] & further-syntactic-patterns]
                     {:pattern  (if (empty? further-syntactic-patterns)
                                  syntactic-pattern
                                  `(!further ~syntactic-pattern [~@(map :pattern further-syntactic-patterns)]))
                      :bindings (->> further-syntactic-patterns
                                     (mapcat :bindings)
                                     ensuring-well-formed-bindings)})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Putting it all together

(sy/defrule pattern'
            (sy/alt any'
                    literal'
                    constant'
                    bind'
                    guard-pattern'
                    view-pattern'
                    or-pattern'
                    and-pattern'
                    seq-pattern'
                    map-pattern'
                    look-in-pattern'
                    variant-pattern'
                    record-pattern'
                    type-pattern'
                    arbitrary-pattern'))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Macros, and their supporting syntax rules

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

(sy/defrule if-match'
            (recap (sy/cat (sy/vec-form (sy/cat pattern' (cap sy/form)))
                           (cap sy/form)
                           (sy/opt (cap sy/form)))
                   (fn
                     ([{:keys [pattern bindings]} [value] [then-action]]
                      `(match* ~value
                               (clauses* ~pattern (fn [~@bindings]
                                                    ~then-action)
                                         !any (fn [] nil))))
                     ([{:keys [pattern bindings]} [value] [then-action] [else-action]]
                      `(match* ~value
                               (clauses* ~pattern (fn [~@bindings]
                                                    ~then-action)
                                         !any (fn [] ~else-action)))))))

(sy/defrule when-match'
            (recap (sy/cat (sy/vec-form (sy/cat pattern' (cap sy/form)))
                           (cap (sy/rep* sy/form)))
                   (fn [{:keys [pattern bindings]} [value] & [actions]]
                     `(match* ~value
                              (clauses* ~pattern (fn [~@bindings]
                                                   (do
                                                     ~@actions))
                                        !any (fn [] nil))))))

(sy/defsyntax clause clause')
(sy/defsyntax clauses clauses')
(sy/defsyntax match match')
(sy/defsyntax try-match try-match')
(sy/defsyntax if-match if-match')
(sy/defsyntax when-match when-match')
