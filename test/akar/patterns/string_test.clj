(ns akar.patterns.string-test
  (:require [akar.patterns.string :refer :all]
            [akar.patterns.basic :refer :all]
            [akar.primitives :refer :all]
            [akar.combinators :refer :all]
            [clojure.test :refer :all]))

(deftest string-patterns-test

  (testing "!regex"
    (let [block (clauses
                  (!regex #"^F (.*) (.*)$") (fn [[handle name]]
                                              {:event  :followed
                                               :handle handle
                                               :name   name})
                  !any (fn [] :bad-event))]

      (testing "captures values from a string that matches regex"
        (is (= {:event  :followed
                :handle "@doofus"
                :name   "Doofus"}
               (try-match "F @doofus Doofus" block))))

      (testing "doesn't match invalid strings"
        (is (= :bad-event
               (try-match "F X" block))))

      (testing "doesn't match non-strings"
        (is (= :bad-event
               (try-match :not-even-a-string block)))))

    (let [block (clauses
                  (!regex #"^F [0-9]{1}$") (fn [] :match)
                  !any (fn [] :no-match))]

      (testing "matches a string against a regex that captures nothing"
        (is (= :match
               (try-match "F 7" block))))

      (testing "doesn't match invalid srings"
        (is (= :no-match
               (try-match "F 11" block)))))))
