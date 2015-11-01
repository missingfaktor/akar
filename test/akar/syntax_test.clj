(ns akar.syntax-test
  (:require [clojure.test :refer :all]
            [akar.primitives :refer :all]
            [akar.patterns :refer :all]
            [akar.combinators :refer :all]
            [n01se.syntax :as sy]
            [akar.syntax :refer :all])
  (:import [java.io StringWriter]))

(deftest syntax-test

  (testing "Translation of primitives:"

    (testing "clause"
      (is (= `(clause* !any (fn [] :val))
             (macroexpand-1 `(clause :_ :val)))))

    (testing "clauses"
      (is (= `(or-else (clause* (!cst 1) (fn [] :one))
                       (clause* !any (fn [] :val)))
             (macroexpand-1 `(clauses 1 :one
                                      :_ :val)))))

    (testing "match"
      (is (= `(match* 3 (or-else (clause* (!cst 1) (fn [] :one))
                                 (clause* !any (fn [] :val))))
             (macroexpand-1 `(match 3
                                    1 :one
                                    :_ :val)))))

    (testing "try-match"
      (is (= `(try-match* 3 (or-else (clause* (!cst 1) (fn [] :one))
                                     (clause* !any (fn [] :val))))
             (macroexpand-1 `(try-match 3
                                        1 :one
                                        :_ :val))))))

  (testing "Translation of patterns:"

    (testing "'any' patterns"
      ; Yes, this is a duplicate test.
      (is (= `(clause* !any (fn [] :val))
             (macroexpand-1 `(clause :_ :val))))
      (is (= `(clause* !any (fn [] :val))
             (macroexpand-1 `(clause :any :val)))))

    (testing "number literals"
      (is (= `(clause* (!cst 2) (fn [] :val))
             (macroexpand-1 `(clause 2 :val)))))

    (testing "string literals"
      (is (= `(clause* (!cst 2) (fn [] :val))
             (macroexpand-1 `(clause 2 :val)))))

    (testing "boolean literals"
      (is (= `(clause* (!cst true) (fn [] :val))
             (macroexpand-1 `(clause true :val)))))

    (testing "keyword literals"
      (is (= `(clause* (!cst :kartofell) (fn [] :val))
             (macroexpand-1 `(clause :kartofell :val)))))

    (testing "nil literal"
      (is (= `(clause* (!cst nil) (fn [] :val))
             (macroexpand-1 `(clause nil :val)))))

    (testing "simple binding"
      (is (= `(clause* !var (fn [x] (inc x)))
             (macroexpand-1 `(clause x (inc x))))))

    (testing "arbitrary pattern functions that emit no values"
      (is (= `(clause* !empty (fn [] :zilch))
             (macroexpand-1 `(clause [!empty] :zilch))))
      (is (= `(clause* (!cst 2) (fn [] :val))
             (macroexpand-1 `(clause [(!cst 2)] :val)))))

    (testing "arbitrary pattern functions that emit values, to be further matched by other patterns"
      (is (= `(clause* (!further !cons [!var (!further !cons [(!cst 2) !var])])
                       (fn [hd tl-1] {:hd hd :tl-1 tl-1}))
             (macroexpand-1 `(clause [!cons hd [!cons 2 tl-1]] {:hd hd :tl-1 tl-1})))))

    (testing "map patterns"
      (let [block (clauses {:sex :f :age :_} :be-polite
                           {:sex :m :age age} age
                           {} :umm)]
        (is (= :be-polite
               (match* {:sex :f :age 30} block)))
        (is (= 25
               (match* {:sex :m :age 25} block)))
        (is (= :umm
               (match* {} block)))))

    (testing "seq patterns"
      (let [block (clauses (:seq [1 2]) :one-two
                           (:seq [1 x]) x
                           (:seq []) :empty
                           (:seq [:_ :_]) :two
                           :_ :welp)]
        (are [x y] (= x y)
                   :one-two (match* [1 2] block)
                   3 (match* [1 3] block)
                   :empty (match* [] block)
                   :two (match* [4 5] block)
                   :welp (match* [4 5 6] block))))

    (testing "seq patterns, with regex"
      (let [!address (!regex #"^(.*), (.*)$")
            block (clauses [!address (:seq [street house-nr])] {:street   street
                                                                :house-nr house-nr}
                           :_ :invalid-address)]
        (is (= {:street "Goblinstraße" :house-nr "21"}
               (match* "Goblinstraße, 21" block)))
        (is (= :invalid-address
               (match* "Gondwanaland" block)))))

    (testing "guard patterns"
      (let [block (clauses (:guard a odd?) a
                           (:guard :_ even?) :nope)]
        (is (= 5
               (match* 5 block)))
        (is (= :nope
               (match* 4 block)))))

    (testing "at-patterns"
      (is (= [[3 4] 3]
             (match [3 4]
                    (:as a [!cons hd :_]) [a hd])))))

  (testing "Sensible syndoc"

    (testing "No terminal should be marked as a rule"
      (is (let [writer (StringWriter.)
                _ (binding [*out* writer]
                    (sy/syndoc match))
                doc (.toString writer)]
            (not (.contains doc "#<")))))))
