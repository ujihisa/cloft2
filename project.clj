(defproject cloft2 "0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/ujihisa/cloft2"
  :license {:name "GPLv3+"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :main ^:skip-aot cloft2.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
