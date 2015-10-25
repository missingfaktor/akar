(ns akar.further-test
  (:require [clojure.test :refer :all]
            [akar.patterns.basic :refer :all]
            [akar.patterns.collection :refer :all]
            [akar.patterns.string :refer :all]
            [akar.further :refer :all]
            [akar.primitives :refer :all]))

(deftest further-test

  (testing "!further"
    (let [block (clauses
                  (!further !cons !var !var) (fn [hd tl]
                                               {:hd hd
                                                :tl tl}))]
      (testing "'furthers' a pattern"
        (is (= {:hd 3 :tl [4 5]}
               (match [3 4 5] block))))))

  (testing "!further-many"
    (let [!address (!regex #"(.*) (.*), (.*)")
          !berlin (!cst "Berlin")
          block (clauses
                  (!further-many !address [!var !var !berlin]) (fn [street house-nr]
                                                                 {:street   street
                                                                  :house-nr house-nr}))]
      (testing "'furthers' a pattern into variadic patterns list"
        (is (= {:street "Jahnstraße" :house-nr "21"}
               (try-match "Jahnstraße 21, Berlin" block)))
        (is (= clause-not-applied
               (try-match "Jahnstraße 21, Hamburg" block)))))))
