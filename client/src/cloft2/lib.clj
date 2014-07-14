(ns cloft2.lib
  (:import [org.bukkit Bukkit]))

(defn sec [n]
  (int (* 20 n)))

(let [plugin (-> (Bukkit/getPluginManager) (.getPlugin "cloft2"))]
  (defn later* [tick f]
    (.scheduleSyncDelayedTask
      (org.bukkit.Bukkit/getScheduler) plugin f tick)))
(defmacro later [tick & exps]
  `(later* ~tick (fn [] ~@exps)))

[(.getName *ns*) 'SUCCESSFULLY-COMPLETED]
