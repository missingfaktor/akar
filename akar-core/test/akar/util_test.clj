(ns akar.util-test
  (:require [akar.util :refer :all]
            [clojure.test :refer :all]
            [akar.syntax :refer :all]))

(deftest postwalk-fn-replace-test
  (testing "should replace the expression with result of replacing fn"
    (let [postwalk-fn-replace #'akar.util/postwalk-fn-replace]
      (is (= (postwalk-fn-replace (fn [expr]
                                       (= :a expr))
                                     (fn [expr]
                                       :b)
                                     '(:a :b :c :d))
             '(:b :b :c :d))))))


(deftest defn-trampolined-test
  (testing "should replace single trampolined-recur call with trampolined function giving correct result"
    (defn-trampolined tail-recursive-sum [x running-total]
      (match x
             0  running-total
             :_ (trampolined-recur (dec x) (+ running-total x))))
    (is (= (tail-recursive-sum 10 100)
           155))
    ;; These values will cause a StackOverflowError if called without defn-trampolined.
    (is (= (tail-recursive-sum 123038 10)
           7569236251)))

  (testing "should replace all the trampolined-recur calls with trampolined function giving correct result"
    (defn-trampolined multi-tail-recursive-fn [x y]
      (match x
             0  y
             ;; Some random logic here just to have more than one trampolined-recur.
             ;; Skips number 4 for addition.
             5 (trampolined-recur (- x 2) (+ y x))
             :_ (trampolined-recur (dec x) (+ y x))))
    (is (= (multi-tail-recursive-fn 3 0)
           6))
    (is (= (multi-tail-recursive-fn 10 0)
           51))
    (is (= (multi-tail-recursive-fn 10 1)
           52))
    (is (= (multi-tail-recursive-fn 123038 10)
           7569236247)))

  (testing "should not evaluate the side affecting function more than required no of times"
    (let [some-global-state (atom 0)]
      (defn-trampolined side-affecting-tail-recursive-sum [some-state x running-total]
        (match x
               0  running-total
               :_ (do
                    (swap! some-state inc)
                    (trampolined-recur some-state (dec x) (+ running-total x)))))
      ;; verify that side affecting operation is not called unless needed
      (is (= (side-affecting-tail-recursive-sum some-global-state 0 10)
             10))
      (is (= 0 @some-global-state))
      ;; verify that the side affecting operation is not called more than expected no of times
      (is (= (side-affecting-tail-recursive-sum some-global-state 5 10)
             25))
      (is (= 5 @some-global-state))))

  (testing "should not fail for a function with no trampoline-recur occurrence"
    (defn-trampolined non-tail-recursive-fn [x y]
      (match x
             0  y
             :_ x))
    (is (= (non-tail-recursive-fn 10 20)
           10))
    (is (= (non-tail-recursive-fn 0 20)
           20))))
