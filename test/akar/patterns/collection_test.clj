(ns akar.patterns.collection-test
  (:require [akar.patterns.collection :refer :all]
            [akar.patterns.basic :refer :all]
            [akar.primitives :refer :all]
            [akar.combinators :refer :all]
            [akar.special-operators :refer :all]
            [clojure.test :refer :all]))

(deftest collection-patterns-test
  (let [block (clauses
                !empty (fn [] :empty)
                !cons (fn [hd tl] {:hd hd :tl tl})
                !any (fn [] :not-sequential))]

    (testing "!empty"
      (is (= :empty
             (match [] block))))

    (testing "!cons"
      (is (= {:hd 3 :tl [4 5]}
             (match [3 4 5] block))))

    (testing "non-sequential data fallthrough for both !empty and !cons"
      (is (= :not-sequential
             (match :some-random-data block)))))

  (let [block (clauses
                (!further-many !seq [!var !any !var]) (fn [a b] [a b]))]
    (testing "!seq"
      (is (= [2 4]
             (match [2 3 4] block)))))

  (let [block (clauses
                (!and (!key :k) (!optional-key :l) (!optional-key :m)) (fn [a b c] [a b c])
                !any (fn [] :stuff))]

    (testing "!key and !optional-key"
      (is (= [:x :y nil]
             (match {:k :x :l :y} block)))

      (is (= :stuff
             (match [] block)))))

  (let [block (clauses
                (!further (!variant :add) [(!cst 0) !var]) (fn [y] [:num y])
                (!further (!variant :sub) [!var (!cst 0)]) (fn [x] [:num x])
                (!at (!variant :num)) (fn [node _] node))]

    (testing "!variant"
      (is (= [:num 3]
             (match [:add 0 3] block)))
      (is (= [:num 5]
             (match [:sub 5 0] block)))
      (is (= [:num 11]
             (match [:num 11] block))))))
