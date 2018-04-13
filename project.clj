(defproject akar "2.0.0"
  :description "First-class patterns for Clojure"
  :url "www.github.com/missingfaktor/akar"
  :license {:name         "Apache License 2.0"
            :url          "http://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo}
  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                    :sign-releases false}]]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [akar/akar-core "2.0.1"]
                 [akar/akar-exceptions "0.0.2"]
                 [akar/akar-commons "0.0.1"]]
  :plugins [[lein-sub "0.3.0"]]
  :sub ["akar-core"])
