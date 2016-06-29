(ns akar.combinators-test
  (:require [clojure.test :refer :all]
            [akar.patterns :refer :all]
            [akar.combinators :refer :all]
            [akar.primitives :refer :all]))

(deftest combinators-test

  (testing "!and"

    (testing "fails if any of the patterns fails"
      (is (= nil
             ((!and !any !bind !fail) 9))))

    (testing "emits all the values, when all patterns succeed"
      (is (= [9 9]
             ((!and !any !bind !bind) 9))))

    (testing "succeeds if no patterns are available"
      (is (= []
             ((!and) 9)))))

  (testing "!or"

    (testing "fails if no pattern succeeds"
      (is (= nil
             ((!or !fail !fail !fail) 9))))

    (testing "emits values from the first matched pattern"
      (is (= []
             ((!or !any !bind) 9)))
      (is (= [9]
             ((!or !bind !any) 9))))

    (testing "fails if no pattern is available"
      (is (= nil
             ((!or) 9)))))

  (testing "!not"

    (testing "reverses a good match"
      (is (= nil
             ((!not !bind) 9))))

    (testing "reverses a bad match"
      (is (= []
             ((!not !fail) 9)))))

  (testing "!at"

    (testing "if matched, gives the same input"
      (is (= 2
             (try-match* 2 (clauses*
                             (!at (!pred even?)) (fn [x] x))))))

    (testing "if not matched, gives nothing"
      (is (= clause-not-applied
             (try-match* 3 (clauses*
                             (!at (!pred even?)) (fn [x] x)))))))

  (testing "!guard"

    (testing "original pattern succeeds, only if guard succeeds too"
      (is (= :even-and-2
             (try-match* 2 (clauses*
                             (!guard (!pred even?) (partial = 2)) (fn [] :even-and-2))))))

    (testing "original pattern fails, if the guard fails"
      (is (= clause-not-applied
             (try-match* 4 (clauses*
                             (!guard (!pred even?) (partial = 2)) (fn [] :even-and-2)))))))

  (testing "!view"

    (let [block (clauses*
                  (!view inc !bind) (fn [x] x))]
      (is (= 10
             (match* 9 block)))))

  (testing "!further"
    (let [block (clauses*
                  (!further !cons [!bind !bind]) (fn [hd tl]
                                                   {:hd hd
                                                    :tl tl}))]
      (testing "'furthers' a pattern"
        (is (= {:hd 3 :tl [4 5]}
               (match* [3 4 5] block))))))

  (testing "!further-many"

    (testing "'furthers' a pattern into variadic patterns list"
      (let [block (clauses*
                    (!further-many !seq [!bind !any !true !bind]) (fn [x y] [x y]))]
        (is (= [1 3]
               (try-match* [1 :whatevs true 3] block)))
        (is (= clause-not-applied
               (try-match* [1 :whatevs false] block)))))

    (testing "supports 'rest' patterns"
      (let [block (clauses*
                    (!further-many !seq [!bind !bind] !bind) (fn [a b rest]
                                                               [a b rest]))]
        (is (= [3 4 [5 6]]
               (try-match* [3 4 5 6] block)))
        (is (= [3 4 []]
               (try-match* [3 4] block)))
        (is (= clause-not-applied
               (try-match* [3] block)))))))
