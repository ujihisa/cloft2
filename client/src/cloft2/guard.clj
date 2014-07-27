(ns cloft2.guard
  (:use [cloft2.lib :only (later sec)])
  (:import [org.bukkit Bukkit Material]
           [org.bukkit.event.entity EntityDamageByEntityEvent]))

(def guard-id-table (atom {}))

(defn EntityDamageByEntityEvent [^org.bukkit.event.entity.EntityDamageByEntityEvent evt]
  )

[(.getName *ns*) 'SUCCESSFULLY-COMPLETED]
; vim: set lispwords+=later :
