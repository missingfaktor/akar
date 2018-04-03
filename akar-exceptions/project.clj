(defproject akar/akar-exceptions "1.0.0"
  :description "First-class patterns for Clojure – Exception handling"
  :url "www.github.com/missingfaktor/akar"
  :license {:name         "Apache License 2.0"
            :url          "http://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo}
  :repositories [["clojars" {:url           "https://clojars.org/akar-exceptions"
                             :sign-releases false}]]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [n01se/seqex "2.0.1"]]
  :profiles {:dev {:plugins [[jonase/eastwood "0.2.3"]]}}
  :aliases {"et" ["do" ["eastwood"] ["test"]]})