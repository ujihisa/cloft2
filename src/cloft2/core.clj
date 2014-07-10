(ns cloft2.core
  (:require [clojure.string :as s]
            [clojure.tools.nrepl.server :as nrepl.server])
  (:gen-class
    :name io.github.ujihisa.cloft2.ClojurePlugin
    :extends org.bukkit.plugin.java.JavaPlugin
    :implements [org.bukkit.event.Listener]
    :exposes-methods {onEnable -onEnable}))

(defn -onEnable [self]
  (prn 'onEnable self))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
