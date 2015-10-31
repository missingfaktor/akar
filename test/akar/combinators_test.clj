(ns akar.combinators-test
  (:require [clojure.test :refer :all]
            [akar.patterns.basic :refer :all]
            [akar.patterns.collection :refer :all]
            [akar.combinators :refer :all]
            [akar.primitives :refer :all]))

(deftest combinators-test

  (testing "!and"

    (testing "fails if any of the patterns fails"
      (is (= nil
             ((!and !any !var !fail) 9))))

    (testing "emits all the values, when all patterns succeed"
      (is (= [9 9]
             ((!and !any !var !var) 9)))))

  (testing "!or"

    (testing "fails if no pattern succeeds"
      (is (= nil
             ((!or !fail !fail !fail) 9))))

    (testing "emits values from the first matched pattern"
      (is (= []
             ((!or !any !var) 9)))
      (is (= [9]
             ((!or !var !any) 9)))))

  (testing "!not"

    (testing "reverse a good match"
      (is (= nil
             ((!not !var) 9))))

    (testing "reverses a bad match"
      (is (= []
             ((!not !fail) 9))))))
