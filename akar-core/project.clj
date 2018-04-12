(defproject akar/akar-core "2.0.0"
            :description "First-class patterns for Clojure – Core framework"
            :url "www.github.com/missingfaktor/akar"
            :license {:name         "Apache License 2.0"
                      :url          "http://www.apache.org/licenses/LICENSE-2.0"
                      :distribution :repo}
            :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                              :sign-releases false}]]
            :dependencies [[org.clojure/clojure "1.9.0"]
                           [n01se/seqex "2.0.2"]]
            :pedantic? :abort
            :main akar.try-out)
