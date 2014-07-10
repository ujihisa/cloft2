(defproject cloft2 "0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "GPLv3+"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :main ^:skip-aot cloft2.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
