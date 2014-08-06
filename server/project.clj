(defproject cloft2 "1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/ujihisa/cloft2"
  :license {:name "GNU GPL v3+"
            :url "http://www.gnu.org/licenses/gpl-3.0.en.html"}
  :repositories {"org.bukkit"
                 "http://repo.bukkit.org/service/local/repositories/snapshots/content/"}
  :dependencies [[org.bukkit/bukkit "1.7.10-R0.1-SNAPSHOT"]
                 [org.bukkit/craftbukkit "1.7.10-R0.1-SNAPSHOT"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [org.clojure/core.incubator "0.1.3"]
                 #_[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.match "0.2.0"]
                 [clj-http "0.9.2"]]
  :main org.bukkit.craftbukkit.Main
  :target-path "target/%s"
  :java-source-paths ["javasrc"]
  :profiles {:uberjar {:aot :all}}
  :min-lein-version "2.2.0")
