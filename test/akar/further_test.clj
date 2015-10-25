(ns akar.further-test
  (:require [clojure.test :refer :all]
            [akar.patterns.basic :refer :all]
            [akar.patterns.collection :refer :all]
            [akar.further :refer :all]
            [akar.primitives :refer :all]))

(deftest further-test

  (testing "!further"

    (testing "'furthers' patterns"
      (is (= {:hd 3 :tl [4 5]}
             (match [3 4 5] (clauses
                              (!further !cons !var !var) (fn [hd tl]
                                                           {:hd hd
                                                            :tl tl}))))))))
