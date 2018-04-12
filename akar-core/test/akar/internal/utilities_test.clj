(ns akar.internal.utilities-test
  (:require [akar.internal.utilities :refer :all]
            [clojure.test :refer :all]))

(deftest internal-utilities-test

  (testing "append"

    (testing "for list"
      (is (= '(3 4 5)
             (append '(3 4) 5))))

    (testing "for vector"
      (is (= [3 4 5]
             (append [3 4] 5)))))

  (testing "clump-after"

    (is (= [3 4 [5 6]]
           (clump-after 2 [3 4 5 6])))
    (is (= [3 4 []]
           (clump-after 2 [3 4]))))

  (testing "same-size?"

    (is (= true
           (same-size? [1 2] [3 4])))
    (is (= true
           (same-size? [] [])))
    (is (= false
           (same-size? [1] [2 3])))))
