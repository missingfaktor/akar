(defproject akar/akar-core "3.0.0"
  :description "First-class patterns for Clojure – Core framework"
  :url "www.github.com/missingfaktor/akar"
  :license {:name         "Apache License 2.0"
            :url          "http://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo}
  :deploy-repositories [["clojars" :clojars]]
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [n01se/seqex "2.0.2"]
                 [akar/akar-commons "3.0.0"]]
  :pedantic? :abort
  :main akar.try-out)
