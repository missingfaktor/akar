(ns akar.primitives-test
  (:require [clojure.test :refer :all]
            [akar.primitives :refer :all]
            [akar.patterns :refer :all]))

(deftest primitives-test

  (testing "clause"

    (testing "invokes no action if pattern did not match"
      (is (= clause-not-applied
             ((clause* !fail (fn [] :some-value)) :value))))

    (testing "invokes the action on the captured value if pattern matched"
      (is (= 6
             ((clause* !bind (fn [x] (* 2 x))) 3))))

    (testing "allows nil values to be returned from the action"
      (is (= nil
             ((clause* !bind (fn [x] nil)) 3)))))

  (testing "clauses"

    (testing "runs through the clauses in order"
      (let [!zero (!pred zero?)
            !odd (!pred odd?)
            block (clauses*
                    !zero (fn [] :zero)
                    !odd (fn [] :odd))]
        (testing "runs through the clauses in order"
          (is (= :zero
                 (match* 0 block)))
          (is (= :odd
                 (match* 9 block)))))))

  (testing "match"

    (testing "throws an error if pattern did not match"
      (is (thrown? RuntimeException
                   (match* :some-value (clauses*))))))

  (testing "try-match"

    (testing "returns nil if pattern did not match"
      (is (= clause-not-applied
             (try-match* :some-value (clauses*))))))

  (testing "or-else"
    (let [[!4 !5 !6] (map !constant [4 5 6])
          !4-or-!5 (or-else !4 !5)
          !4-or-!5-or-!6 (or-else !4 !5 !6)]
      (is (= :any
             (match* 6 (clauses*
                         !4-or-!5 (fn [] :num)
                         !any (fn [] :any)))))
      (is (= :num
             (match* 6 (clauses*
                         !4-or-!5-or-!6 (fn [] :num)
                         !any (fn [] :num))))))))
