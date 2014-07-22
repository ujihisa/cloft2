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
           [org.bukkit.entity FallingBlock]
           [org.bukkit.inventory ItemStack]))

#_(def chained-target-materials
  #{Material/LOG Material/LOG_2
    Material/LEAVES Material/LEAVES_2})

#_(def axe-materials
  #{Material/IRON_AXE Material/WOOD_AXE Material/STONE_AXE
    Material/DIAMOND_AXE Material/GOLD_AXE})

; TODO move it to lib
(defn- block-above [^Location block]
  (-> block .getLocation (l/add-loc 0 1 0) .getBlock))

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
        (kickory (block-above b) (dec limit))))))

(def block-breaking-tick? (ref false))
(defn BlockBreakEvent [^org.bukkit.event.block.BlockBreakEvent evt ^Block block]
  (dosync (ref-set block-breaking-tick? true))
  (let [player (-> evt .getPlayer)]
    (when (and
            (#{Material/LOG Material/LOG_2} (-> block .getType))
            (every? #(-> % .getType .isSolid not) (l/neighbours block)))
      (kickory (block-above block) 100)))
  (later 0
    (dosync (ref-set block-breaking-tick? false))))

(defn BlockPhysicsEvent [^org.bukkit.event.block.BlockPhysicsEvent evt ^Block block]
  (when (and (#{Material/LEAVES Material/LEAVES_2} (-> block .getType))
             @block-breaking-tick?
             (.isEmpty (l/block-below block))
             (every? #(not (#{Material/LOG Material/LOG_2} (.getType %)))
                     (l/neighbours block)))
    (let [sapling (ItemStack. Material/SAPLING 1 (short 0) (.getData block))
          stick (ItemStack. Material/STICK 1)
          apple (ItemStack. Material/APPLE 1)
          golden-apple (ItemStack. Material/APPLE 1)
          itemstack (if (= 0 (.getData block))
                      (rand-nth [sapling apple apple golden-apple stick stick stick stick])
                      (rand-nth [sapling sapling stick stick stick stick stick stick]))]
      (l/drop-item (-> block .getLocation) itemstack))
    (let [f (l/fall block)]
      (later 0
        (.setVelocity f (Vector. (rand-nth [0.5 0.0 -0.5])
                                 (inc (rand))
                                 (rand-nth [0.5 0.0 -0.5])))))))

[(.getName *ns*) 'SUCCESSFULLY-COMPLETED]
; vim: set lispwords+=later :
