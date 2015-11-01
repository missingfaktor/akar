(ns akar.special-operators-test
  (:require [clojure.test :refer :all]
            [akar.patterns :refer :all]
            [akar.special-operators :refer :all]
            [akar.primitives :refer :all]))

(deftest special-operators-test

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

  (testing "!further"
    (let [block (clauses*
                  (!further !cons [!var !var]) (fn [hd tl]
                                                 {:hd hd
                                                  :tl tl}))]
      (testing "'furthers' a pattern"
        (is (= {:hd 3 :tl [4 5]}
               (match* [3 4 5] block))))))

  (testing "!further-many"

    (testing "'furthers' a pattern into variadic patterns list"
      (let [!address (!regex #"(.*) (.*), (.*)")
            !berlin (!cst "Berlin")
            block (clauses*
                    (!further-many !address [!var !var !berlin]) (fn [street house-nr]
                                                                   {:street   street
                                                                    :house-nr house-nr}))]
        (is (= {:street "Jahnstraße" :house-nr "21"}
               (try-match* "Jahnstraße 21, Berlin" block)))
        (is (= clause-not-applied
               (try-match* "Jahnstraße 21, Hamburg" block)))))

    (testing "supports 'rest' patterns"
      (let [block (clauses*
                    (!further-many !seq [!var !var] !var) (fn [a b rest]
                                                            [a b rest]))]
        (is (= [3 4 [5 6]]
               (try-match* [3 4 5 6] block)))
        (is (= [3 4 []]
               (try-match* [3 4] block)))
        (is (= clause-not-applied
               (try-match* [3] block)))))))
