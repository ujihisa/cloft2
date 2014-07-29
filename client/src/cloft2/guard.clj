(ns cloft2.guard
  (:use [cloft2.lib :only (later sec)])
  (:import [org.bukkit Bukkit Material]))

(def guard-id-table (atom {}))

(defn EntityDamageByEntityEvent [^org.bukkit.event.entity.EntityDamageByEntityEvent evt]
  )

; vim: set lispwords+=later :
