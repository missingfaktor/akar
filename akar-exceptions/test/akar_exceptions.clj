(ns akar-exceptions
  (:require [clojure.test :refer :all]
            [akar-exceptions.core :refer :all]))

(deftest akar-exceptions-core-test

  (testing "attempt"

    (testing "returns value on successful computation"
      (is (= :ok
             (attempt (do
                        (/ 0 2)
                        :ok)
                      :on-error ((:type Exception) :ko)))))

    (testing "skips exception handlers on successful computation"
      (let [handler-invoked (atom false)
            _               (attempt (do
                                       (/ 0 2))
                                     :on-error (:_ (reset! handler-invoked true)))]
        (is (= false
               @handler-invoked)))

      (testing "runs finally on successful execution"
        (let [finally-invoked (atom false)
              _               (attempt (do
                                         (/ 0 2))
                                       :on-error (:_ :ko)
                                       :ultimately (reset! finally-invoked true))]
          (is (= true
                 @finally-invoked))))

      (testing "handles exceptions using given handler"
        (is (= :ko
               (attempt (do
                          (/ 2 0)
                          :ok)
                        :on-error ((:type Exception) :ko)))))

      (testing "uses super-class handlers if available"
        (is (= :arith
               (attempt (do
                          (/ 2 0)
                          :ok)
                        :on-error ((:type ArithmeticException) :arith
                                    (:type NumberFormatException) :number-format))))
        (is (= :number-format
               (attempt (do
                          (Integer/parseInt "not-really-a-number")
                          :ok)
                        :on-error ((:type ArithmeticException) :arith
                                    (:type NumberFormatException) :number-format))))
        (is (= :wut
               (attempt (do
                          (count (count 1))
                          :ok)
                        :on-error ((:type ArithmeticException) :arith
                                    (:type NumberFormatException) :number-format
                                    (:type Exception) :wut)))))

      (testing "runs finally on failed execution"
        (let [finally-invoked (atom false)
              _               (attempt (do
                                         (/ 2 0))
                                       :on-error (:_ :ko)
                                       :ultimately (reset! finally-invoked true))]
          (is (= true
                 @finally-invoked)))))))
