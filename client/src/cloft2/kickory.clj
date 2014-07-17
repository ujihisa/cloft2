(ns cloft2.kickory
  (:use [clojure.core.strint :only (<<)]
        [cloft2.lib :only (later sec)])
  (:require [clj-http.client]
            [clojure.string :as s])
  (:import [org.bukkit Bukkit Material Location]
           [org.bukkit.block Block]
           [org.bukkit.util Vector]
           [org.bukkit.event HandlerList]
           [org.bukkit.entity FallingBlock]
           [org.bukkit.inventory ItemStack]))

#_(def chained-target-materials
  #{Material/LOG Material/LOG_2
    Material/LEAVES Material/LEAVES_2})

#_(def axe-materials
  #{Material/IRON_AXE Material/WOOD_AXE Material/STONE_AXE
    Material/DIAMOND_AXE Material/GOLD_AXE})

(defn- add-loc [^Location loc x y z]
  (doto (-> loc .clone) (.add x y z)))

; TODO move it to lib
(defn- block-above [^Location block]
  (-> block .getLocation (add-loc 0 1 0) .getBlock))

(defn- neighbours [^Block block]
  (for [x (range -1 2)
        z (range -1 2)
        :when (not (= x 0 z))]
    (-> block .getLocation (add-loc x 0 z) .getBlock)))

(defn- fall [^Block block]
  (let [world (-> block .getWorld)
        loc (-> block .getLocation)
        material (-> block .getType)
        data (-> block .getData)]
    (-> block (.setType Material/AIR))
    (-> block (.setData 0))
    (-> world (.spawnFallingBlock loc material data))))

(defn- kickory [^Block block limit]
  (when (< 0 limit)
    (doseq [b (conj (neighbours block) block)
            :when (#{Material/LOG Material/LOG_2} (-> b .getType))]
      (if (= 0 (rand-int 2))
        (let [falling (fall b)]
          (later 0
            (.setVelocity falling (Vector. 0.1 0.3 0.1))))
        (.breakNaturally b (ItemStack. Material/WOOD_AXE 1)))
      (do
        (kickory b (dec limit))
        (kickory (block-above b) (dec limit))))))

(defn BlockBreakEvent [evt ^Block block]
  (let [player (-> evt .getPlayer)]
    (when (and
            (#{Material/LOG Material/LOG_2} (-> block .getType))
            (every? #(-> % .getType .isSolid not) (neighbours block)))
      (kickory (block-above block) 100))))

[(.getName *ns*) 'SUCCESSFULLY-COMPLETED]
; vim: set lispwords+=later :
