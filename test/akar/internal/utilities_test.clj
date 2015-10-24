(ns akar.internal.utilities-test
  (:require [akar.internal.utilities :refer :all]
            [clojure.test :refer :all]))

(deftest internal-utilities-test
  (testing "variadic-reducive-function"
    (let [f (variadic-reducive-function :zero 0
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
               18))))))
