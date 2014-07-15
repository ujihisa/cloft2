(ns cloft2.kickory
  (:use [clojure.core.strint :only (<<)]
        [cloft2.lib :only (later sec)])
  (:require [clj-http.client]
            [clojure.string :as s])
  (:import [org.bukkit Bukkit Material]
           [org.bukkit.event HandlerList]
           [org.bukkit.inventory ItemStack]))

#_(prn (Material/matchMaterial "%WOOD%"))
(def tree-materials #{Material/LOG})

(defn BlockBreakEvent [evt block]
  (let [player (-> evt .getPlayer)]
    nil))

; vim: set lispwords+=later :
