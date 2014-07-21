(ns cloft2.lib
  (:import [org.bukkit Bukkit Material Location ChatColor]
           [org.bukkit.block Block]
           [org.bukkit.entity Entity]
           [org.bukkit.inventory ItemStack]
           [org.bukkit.util Vector]))

(let [recent-msgs (atom [])]
  (defn post-lingr [msg & [msgtype]]
    (when-not (and msgtype (= msgtype
                              (-> @recent-msgs first first)
                              (-> @recent-msgs second first)))
      (swap! recent-msgs
             (fn [orig msg]
               (cons
                 [msgtype msg]
                 (if (< 10 (count orig)) (drop-last orig) orig)))
             msg)
      (clj-http.client/post
        "http://lingr.com/api/room/say"
        {:form-params
         {:room "mcujm"
          :bot 'sugoicraft
          :text (ChatColor/stripColor (str msg))
          :bot_verifier "bb5060f31bc6e89018c55ac72d39d5ca6aca75c9"}}))))

(defn sec [n]
  (int (* 20 n)))

(let [plugin (-> (Bukkit/getPluginManager) (.getPlugin "cloft2"))]
  (defn later* [tick f]
    (.scheduleSyncDelayedTask
      (org.bukkit.Bukkit/getScheduler) plugin f tick)))
(defmacro later [tick & exps]
  `(later* ~tick (fn [] ~@exps)))

(defn fall [^Block block]
  (let [world (-> block .getWorld)
        loc (-> block .getLocation)
        material (-> block .getType)
        data (-> block .getData)]
    (-> block (.setType Material/AIR))
    (-> block (.setData 0))
    (-> world (.spawnFallingBlock loc material data))))

(defn block-below [block]
  (-> (doto (.getLocation block) (.add 0 -1 0))
    .getBlock))

(defn add-loc [^Location loc x y z]
  (doto (-> loc .clone) (.add x y z)))

(defn neighbours [^Block block]
  (for [x (range -1 2)
        z (range -1 2)
        :when (not (= x 0 z))]
    (-> block .getLocation (add-loc x 0 z) .getBlock)))

(defn drop-item [^Location loc ^ItemStack itemstack]
  (.dropItemNaturally (.getWorld loc) loc itemstack))

(defn block-set [^Block block ^Material material ^Byte data]
  (.setType block material)
  (.setData block data))

(defn set-velocity [^Entity entity ^Double x ^Double y ^Double z]
  (.setVelocity entity (Vector. x y z)))

[(.getName *ns*) 'SUCCESSFULLY-COMPLETED]
