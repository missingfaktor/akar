(ns akar.combinators-test
  (:use [clojure.test :refer :all]
        [akar.patterns.basic :refer :all]
        [akar.combinators :refer :all]
        [akar.primitives :refer :all]))

(deftest and-combinator

  (testing "fails if any of the patterns fails"
    (is (= nil
          ((!and !any !var !pfail) 9))))

  (testing "gives all the extracts when all patterns succeed"
    (is (= [9 9]
           ((!and !any !var !var) 9)))))

(deftest or-combinator

  (testing "fails if no pattern succeeds"
    (is (= nil
           ((!or !pfail !pfail !pfail) 9))))

  (testing "gives extracts from the first matched pattern"
    (is (= []
           ((!or !any !var) 9)))
    (is (= [9]
           ((!or !var !any) 9)))))

(deftest not-combinator

  (testing "reverse a good match"
    (is (= nil
           ((!not !var) 9))))

  (testing "reverses a bad match"
    (is (= []
           ((!not !pfail) 9)))))
