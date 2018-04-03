(ns akar.internal.utilities-test
  (:require [akar.internal.utilities :refer :all]
            [clojure.test :refer :all]))

(deftest internal-utilities-test

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
           (same-size? [1] [2 3]))))

  (testing "single"

    (is (= 3
           (single [3])))
    (is (thrown? RuntimeException
                 (single [1 4])))
    (is (thrown? RuntimeException
                 (single [])))))
