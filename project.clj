(defproject akar "3.0.0"
  :description "First-class patterns for Clojure"
  :url "www.github.com/missingfaktor/akar"
  :license {:name         "Apache License 2.0"
            :url          "http://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo}
  :deploy-repositories [["clojars" :clojars]]
  :dependencies [[org.clojure/clojure "1.11.3"]
                 [akar/akar-core "3.0.0"]
                 [akar/akar-exceptions "3.0.0"]
                 [akar/akar-commons "3.0.0"]]
  :plugins [[lein-sub "0.3.0"]]
  :sub ["akar-core"])
