(defproject akar "0.1.0-SNAPSHOT"
  :description "First-class patterns for Clojure"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [n01se/seqex "2.0.1"]]
  :profiles {:dev {:plugins [[jonase/eastwood "0.2.1"]]}}
  :aliases  {"et" ["do" ["eastwood"] ["test"]]}
  :main akar.try-out
)
