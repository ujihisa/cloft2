(ns cloft2.safe-grass
  (:use [clojure.core.strint :only (<<)]
        [cloft2.lib :only (later sec)])
  (:require [cloft2.lib :as l])
  (:import [org.bukkit Bukkit Material]
           [org.bukkit.event HandlerList]
           [org.bukkit.event.entity EntityDamageEvent$DamageCause]))

(defn EntityDamageEvent [evt entity]
  (condp = (.getCause evt)
    EntityDamageEvent$DamageCause/FALL
    (let [block-below (l/block-below (-> entity .getLocation .getBlock))]
      (when (= Material/GRASS (-> block-below .getType))
        (-> evt (.setCancelled true))
        (-> block-below (.setType Material/DIRT))
        (.setVelocity entity (let [v (.getVelocity entity)]
                               (.setY v (+ 0.5 (.getY v)))
                               v))))
    nil))

[(.getName *ns*) 'SUCCESSFULLY-COMPLETED]
; vim: set lispwords+=later :
