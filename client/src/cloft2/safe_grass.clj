(ns cloft2.safe-grass
  (:use [clojure.core.strint :only (<<)]
        [cloft2.lib :only (later sec)])
  (:import [org.bukkit Bukkit Material]
           [org.bukkit.event HandlerList]
           [org.bukkit.event.entity EntityDamageEvent$DamageCause]))

(defn- get-block-below [loc]
  (-> (doto (.clone loc) (.add 0 -1 0))
    .getBlock))

(defn EntityDamageEvent [evt entity]
  (condp = (.getCause evt)
    EntityDamageEvent$DamageCause/FALL
    (let [block-below (get-block-below (-> entity .getLocation))]
      (when (= Material/GRASS (-> block-below .getType))
        (-> evt (.setCancelled true))
        (-> block-below (.setType Material/DIRT))))
    #_(let [damager (-> evt .getDamager)])
    nil #_(prn 'EntityDamageEvent :else evt)))

[(.getName *ns*) 'SUCCESSFULLY-COMPLETED]
; vim: set lispwords+=later :
