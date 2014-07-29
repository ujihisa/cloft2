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
           [org.bukkit.entity Entity FallingBlock Player]
           [org.bukkit.inventory ItemStack]))

(def pickaxes #{Material/WOOD_PICKAXE Material/IRON_PICKAXE Material/GOLD_PICKAXE Material/DIAMOND_PICKAXE})

(defn BlockBreakEvent [^org.bukkit.event.block.BlockBreakEvent evt ^Block block]
  (let [player (-> evt .getPlayer)]
    (when (= Material/COAL_ORE (.getType block))
      (.setCancelled evt true)
      (l/block-set block Material/AIR 0)
      (when (contains? pickaxes (-> evt .getPlayer .getItemInHand .getType))
        (l/drop-item (-> block .getLocation) (ItemStack. Material/COAL_ORE 1))
        (l/block-set block Material/FIRE 0)
        (let [f (l/fall block)]
          (.setDropItem f false)
          (later 0
            (l/set-velocity f
                            (- (rand) 0.5) (* 0.1 (inc (rand))) (- (rand) 0.5))))))))

(defn EntityChangeBlockEvent [^org.bukkit.event.entity.EntityChangeBlockEvent evt ^Entity entity]
  (let [block (.getBlock evt)]
    (condp instance? entity
      FallingBlock
      (when (= Material/FIRE (.getMaterial entity))
        (.setCancelled evt true)
        (doseq [player (Bukkit/getOnlinePlayers)]
          (l/send-block-change player (.getLocation block) Material/FIRE 0))
        (later (sec 10)
          (doseq [player (Bukkit/getOnlinePlayers)]
            (l/send-block-change player (.getLocation block) (.getType block) (.getData block)))))
      nil)))

; vim: set lispwords+=later :
