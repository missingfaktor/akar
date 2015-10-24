(ns akar.patterns.string-test
  (:require [akar.patterns.string :refer :all]
            [akar.patterns.basic :refer :all]
            [akar.primitives :refer :all]
            [akar.combinators :refer :all]
            [clojure.test :refer :all]))

(deftest string-patterns-test

  (testing "!regex"
    (let [block (clauses
                  (!regex #"F (.*) (.*)") (fn [[handle name]]
                                            {:event :followed
                                             :handle handle
                                             :name name})
                  !any (fn [] :bad-event))]
      (is (= {:event :followed
              :handle "@doofus"
              :name "Doofus"}
             (try-match "F @doofus Doofus" block)))
      (is (= :bad-event
             (try-match "F X" block))))))
