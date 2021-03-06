(ns cloft2.sneaking-jump
  (:use [cloft2.lib :only (later sec)])
  (:require [cloft2.lib :as l])
  (:import [org.bukkit Material Sound]
           [org.bukkit.entity Player]
           [org.bukkit World]))

(def player-sneak-counter (ref {}))

(defn PlayerToggleSneakEvent [^org.bukkit.event.player.PlayerToggleSneakEvent evt ^Player player]
  (when (.isSneaking evt)
    (dosync
      (ref-set
        player-sneak-counter
        (assoc @player-sneak-counter player
               (-> (@player-sneak-counter player)
                 (or 0) (inc)))))
    (later (sec 1.5)
      (dosync
        (ref-set
          player-sneak-counter
          (assoc @player-sneak-counter player
                 (-> (@player-sneak-counter player)
                   (or 0) (dec))))))
    (when (= 3 (@player-sneak-counter player))
      #_(helper/play-sound (.getLocation player) Sound/BAT_TAKEOFF 0.8 (rand-nth [0.5 0.8 1.2]))
      (let [location (-> player .getLocation)]
        (l/play-sound (.getLocation player) Sound/BAT_TAKEOFF 0.8 1.5))
      (.setFallDistance player 0.0)
      (.setVelocity player (let [v (.getVelocity player)]
                             (.setY v (+ 0.9 (.getY v)))
                             v)))))

(def during-knockback (atom #{}))
(def on-ground (atom #{}))
(defn PlayerMoveEvent [^org.bukkit.event.player.PlayerMoveEvent evt ^Player player]
  (when (and (.isSneaking player)
             (@on-ground player)
             (not (@during-knockback player))
             (> (.getY (.getTo evt)) (.getY (.getFrom evt)))
             (not (.isOnGround player)))
    #_(helper/play-sound (.getLocation player) Sound/BAT_TAKEOFF 0.8 0.5)
    #_(helper/play-sound (.getLocation player) Sound/BAT_TAKEOFF 0.8 1.0)
    (let [location (-> player .getLocation)]
      (l/play-sound (.getLocation player) Sound/BAT_TAKEOFF 0.8 0.5))
    (.setFallDistance player 0.0)
    (.setVelocity player (doto (.getVelocity player)
                           (.setY 0.9))))
  (if (.isOnGround player)
    (when-not (@on-ground player) (swap! on-ground conj player))
    (when (@on-ground player) (swap! on-ground disj player))))

; only to give information to move event that if it was triggered as knockback or not.
(defn EntityDamageEvent [^org.bukkit.event.entity.EntityDamageEvent evt entity]
  (when (instance? Player entity)
    (swap! during-knockback conj entity)
    (later (sec 1)
      (swap! during-knockback disj entity))))

; vim: lispwords+=later :
