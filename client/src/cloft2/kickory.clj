(ns cloft2.kickory
  (:use [clojure.core.strint :only (<<)]
        [cloft2.lib :only (later sec)])
  (:require [clj-http.client]
            [clojure.string :as s]
            [cloft2.lib :as l])
  (:import [org.bukkit Bukkit Material Location]
           [org.bukkit.block Block]
           [org.bukkit.util Vector]
           [org.bukkit.event HandlerList]
           [org.bukkit.entity FallingBlock Entity]
           [org.bukkit.inventory ItemStack]))

#_(def chained-target-materials
  #{Material/LOG Material/LOG_2
    Material/LEAVES Material/LEAVES_2})

#_(def axe-materials
  #{Material/IRON_AXE Material/WOOD_AXE Material/STONE_AXE
    Material/DIAMOND_AXE Material/GOLD_AXE})

(defn- kickory [^Block block limit]
  (when (< 0 limit)
    (doseq [b (conj (l/neighbours block) block)
            :when (#{Material/LOG Material/LOG_2} (-> b .getType))]
      (if (= 0 (rand-int 2))
        (let [falling (l/fall b)]
          (later 0
            (.setVelocity falling (Vector. 0.1 0.3 0.1))))
        (.breakNaturally b (ItemStack. Material/WOOD_AXE 1)))
      (do
        (kickory b (dec limit))
        (kickory (l/block-above b) (dec limit))))))

(def block-breaking-tick? (ref false))
(defn BlockBreakEvent [^org.bukkit.event.block.BlockBreakEvent evt ^Block block]
  (dosync (ref-set block-breaking-tick? true))
  (let [player (-> evt .getPlayer)]
    (when (and
            (#{Material/LOG Material/LOG_2} (-> block .getType))
            (every? #(-> % .getType .isSolid not) (l/neighbours block)))
      (kickory (l/block-above block) 100)))
  (later 0
    (dosync (ref-set block-breaking-tick? false))))

(defn BlockPhysicsEvent [^org.bukkit.event.block.BlockPhysicsEvent evt ^Block block]
  (when (and (#{Material/LEAVES Material/LEAVES_2} (-> block .getType))
             @block-breaking-tick?
             (.isEmpty (l/block-below block))
             (every? #(not (#{Material/LOG Material/LOG_2} (.getType %)))
                     (l/neighbours block)))
    #_(let [sapling (ItemStack. Material/SAPLING 1 (short 0) (.getData block))
          stick (ItemStack. Material/STICK 1)
          apple (ItemStack. Material/APPLE 1)
          golden-apple (ItemStack. Material/APPLE 1)
          material (if (= 0 (.getData block))
                      (rand-nth [sapling apple apple golden-apple stick stick stick stick])
                      (rand-nth [sapling sapling stick stick stick stick stick stick]))]
      #_(l/drop-item (-> block .getLocation) material))
    (let [f (l/fall block)]
      (.setDropItem f false)
      (later 0
        (.setVelocity f (Vector. (rand-nth [0.5 0.0 -0.5])
                                 (inc (rand))
                                 (rand-nth [0.5 0.0 -0.5])))))))

(defn EntityChangeBlockEvent [^org.bukkit.event.entity.EntityChangeBlockEvent evt ^Entity entity]
  (condp instance? entity
    FallingBlock
    (when (contains? #{Material/LEAVES Material/LEAVES_2} (.getTo evt))
      (.setCancelled evt true)
      (let [loc (.getLocation entity)]
        (condp > (rand-int 100)
          1 (l/drop-item loc (ItemStack. Material/APPLE 1))
          2 (l/drop-item loc (ItemStack. Material/COAL 1 (short 0) (byte 1)))
          4 (l/block-set (.getBlock loc) Material/SAPLING (.getBlockData entity))
          20 (l/drop-item loc (ItemStack. Material/STICK 1))
          50 (.setCancelled evt false) ; LEAVES
          nil)))
    nil))

[(.getName *ns*) 'SUCCESSFULLY-COMPLETED]
; vim: set lispwords+=later :
