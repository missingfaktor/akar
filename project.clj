(defproject akar "0.1.0"
  :description "First-class patterns for Clojure"
  :license {:name         "The MIT License"
            :url          "http://opensource.org/licenses/mit-license.php"
            :distribution :repo}
  :signing {:gpg-key "F5EEC7BF"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [n01se/seqex "2.0.1"]]
  :profiles {:dev {:plugins [[jonase/eastwood "0.2.3"]]}}
  :aliases {"et" ["do" ["eastwood"] ["test"]]}
  :main akar.try-out)
