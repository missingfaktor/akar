(ns akar-commons.miscellaneous-test
  (:require [akar-commons.miscellaneous :refer :all]
            [clojure.test :refer :all]))

(deftest akar-commons-miscellaneous-test

  (testing "variadic-reducive-function"
    (let [f (variadic-reductive-function :zero 0
                                         :combine (fn [x y] (+ x y)))]
      (testing "zero"
        (is (= (f)
               0)))

      (testing "one"
        (is (= (f 3)
               3)))

      (testing "two"
        (is (= (f 3 6)
               9)))

      (testing "many"
        (is (= (f 3 6 9 0)
               18)))))
  (testing "single"

    (is (= 3
           (single [3])))
    (is (thrown? RuntimeException
                 (single [1 4])))
    (is (thrown? RuntimeException
                 (single [])))))
