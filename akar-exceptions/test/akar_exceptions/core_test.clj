(ns akar-exceptions.core-test
  (:require [clojure.test :refer :all]
            [akar-exceptions.core :refer :all])
  (:import [clojure.lang ExceptionInfo]))

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

      (testing "bubbles up throwable if a suitable handler is not found"
        (is (thrown? UnsupportedOperationException
                     (attempt (do
                                (count (count 1))
                                :ok)
                              :on-error ((:type ArithmeticException) :arith
                                          (:type NumberFormatException) :number-format)))))

      (testing "runs finally on failed execution"
        (let [finally-invoked (atom false)
              _               (attempt (do
                                         (/ 2 0))
                                       :on-error (:_ :ko)
                                       :ultimately (reset! finally-invoked true))]
          (is (= true
                 @finally-invoked))))

      (testing "is okay with empty handler list"
        (is (attempt (/ 0 2) :on-error ())))))

  (testing "raise"

    (testing "throws regular throwables"
      (is (thrown? NumberFormatException
                   (raise (NumberFormatException.)))))

    (testing "throws map with the given message as ex-info"
      (is (thrown-with-msg? ExceptionInfo #"^hello$"
                            (raise {:message "hello"}))))

    (testing "throws map with no specific message as ex-info"
      (is (thrown? ExceptionInfo
                   (raise {:some-key :some-value}))))
    (testing "throws random values"
      (is (thrown? ExceptionInfo
                   (raise :not-a-throwable-or-a-map)))))

  (testing "pattern functions"
    (let [exec (fn [block]
                 (attempt (block)
                          :on-error ([!ex-info {:occurrences n}] n
                                      [!ex-info :_]              :ex-info-still
                                      [(!ex RuntimeException) e] (.getMessage e))))]
      (is (= 2
             (exec (fn [] (raise {:occurrences 2})))))
      (is (= :ex-info-still
             (exec (fn [] (raise 11)))))
      (is (= "Oops"
             (exec (fn [] (raise (RuntimeException. "Oops"))))))
      (is (thrown? Exception
                   (exec (fn [] (raise (Exception.)))))))))
