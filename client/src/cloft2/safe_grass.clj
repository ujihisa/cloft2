(ns cloft2.safe-grass
  (:use [clojure.core.strint :only (<<)]
        [cloft2.lib :only (later sec)])
  (:require [cloft2.lib :as l])
  (:import [org.bukkit Bukkit Material]
           [org.bukkit.event HandlerList]
           [org.bukkit.event.entity EntityDamageEvent$DamageCause]
           [org.bukkit.entity Entity]))

(defn EntityDamageEvent [^org.bukkit.event.entity.EntityDamageEvent evt ^Entity entity]
  (condp = (.getCause evt)
    EntityDamageEvent$DamageCause/FALL
    (let [block-below (l/block-below (-> entity .getLocation .getBlock))]
      (when (= Material/GRASS (-> block-below .getType))
        (-> evt (.setCancelled true))
        (l/block-set block-below Material/DIRT 0)
        (-> entity (.setVelocity (let [v (.getVelocity entity)]
                                   (.setY v (+ 0.3 (.getY v)))
                                   v)))))
    nil))
; vim: set lispwords+=later :
