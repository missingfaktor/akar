(defproject london.rahul/akar "6.0.0"
  :description "First-class patterns for Clojure"
  :url "www.github.com/missingfaktor/akar"
  :license {:name         "Apache License 2.0"
            :url          "http://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo}
  :deploy-repositories [["clojars" :clojars]]
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [london.rahul/panini "1.0.0" :exclusions [org.clojure/clojure]]
                 [london.rahul/akar-core "6.0.0"]
                 [london.rahul/akar-exceptions "6.0.0"]
                 [london.rahul/akar-commons "6.0.0"]]
  :java-version "25"
  :properties {"maven.compiler.source" "25"
               "maven.compiler.target" "25"
               "java.version" "25"}
  :pom-plugins [[org.apache.maven.plugins/maven-compiler-plugin "3.13.0" {:configuration {:source "25" :target "25"}}]]
  :repl-options {:init-ns akar.try-out}
  :plugins [[lein-sub "0.3.0"]]
  :sub ["akar-commons" "akar-core" "akar-exceptions"]
  :aliases {"test" ["sub" "test"]})
