(defproject akar/akar-exceptions "0.0.2"
            :description "First-class patterns for Clojure – Exception handling"
            :url "www.github.com/missingfaktor/akar"
            :license {:name         "Apache License 2.0"
                      :url          "http://www.apache.org/licenses/LICENSE-2.0"
                      :distribution :repo}
            :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                              :sign-releases false}]]
            :dependencies [[org.clojure/clojure "1.9.0"]
                           [n01se/seqex "2.0.2"]
                           [akar/akar-core "2.0.1"]
                           [akar/akar-commons "0.0.1"]]
            :pedantic? :abort)
