(defproject london.rahul/akar-exceptions "5.0.0"
  :description "First-class patterns for Clojure – Exception handling"
  :url "www.github.com/missingfaktor/akar"
  :license {:name         "Apache License 2.0"
            :url          "http://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo}
  :deploy-repositories [["clojars" :clojars]]
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [london.rahul/akar-core "5.0.0"]
                 [london.rahul/akar-commons "5.0.0"]]
  :java-version "25"
  :properties {"maven.compiler.source" "25"
               "maven.compiler.target" "25"
               "java.version" "25"}
  :pom-plugins [[org.apache.maven.plugins/maven-compiler-plugin "3.13.0" {:configuration {:source "23" :target "23"}}]])
