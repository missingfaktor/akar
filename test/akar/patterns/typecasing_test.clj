(ns akar.patterns.typecasing-test
  (:require [akar.patterns.typecasing :refer :all]
            [akar.patterns.basic :refer :all]
            [akar.primitives :refer :all]
            [akar.combinators :refer :all]
            [clojure.test :refer :all])
  (:import [clojure.lang Keyword]))

(deftest typecasing-patterns-test

  (testing "!class"
    (let [block (clauses
                  (!class String) (fn [] :string)
                  (!class Keyword) (fn [] :keyword))]
      (is (= :string
             (try-match "SomeString" block)))
      (is (= :keyword
             (try-match :some-keyword block)))
      (is (= clause-not-applied
             (try-match 4 block)))))

  (testing "!tag"
    (let [block (clauses
                  (!tag :some-tag) (fn [] :yes))]
      (is (= :yes
             (try-match {:tag :some-tag} block)))))

  (testing "!type"
    (let [block (clauses
                  (!and (!type :card) !var) (fn [card] (:details card)))]
      (is (= "Details"
             (try-match (with-meta {:details "Details"} {:type :card}) block))))))