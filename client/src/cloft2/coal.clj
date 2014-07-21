(ns cloft2.coal
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

(def pickaxes #{Material/WOOD_PICKAXE Material/IRON_PICKAXE Material/GOLD_PICKAXE Material/DIAMOND_PICKAXE})

(defn BlockBreakEvent [^org.bukkit.event.block.BlockBreakEvent evt ^Block block]
  (let [player (-> evt .getPlayer)]
    (when (= Material/COAL_ORE (.getType block))
      (.setCancelled evt true)
      (l/block-set block Material/AIR 0)
      (when (contains? pickaxes (-> evt .getPlayer .getItemInHand .getType))
        (l/drop-item (-> block .getLocation) (ItemStack. Material/COAL_ORE 1))
        (later (sec 1)
          (when (= Material/AIR (.getType block))
            (l/block-set block Material/FIRE 0)
            (let [f (l/fall block)]
              (.setDropItem f false)
              (later 0
                (l/set-velocity f
                                (- (rand) 0.5) (* 0.5 (rand)) (- (rand) 0.5))))))))))

[(.getName *ns*) 'SUCCESSFULLY-COMPLETED]
