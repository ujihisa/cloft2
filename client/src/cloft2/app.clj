(ns cloft2.core
  (:use [clojure.core.strint :only (<<)]
        [cloft2.lib :only (later sec)])
  (:require [clj-http.client]
            [clojure.string :as s]
            [cloft2.lib :as l]
            [cloft2.fast-dash]
            [cloft2.sneaking-jump]
            [cloft2.safe-grass]
            [cloft2.kickory]
            [cloft2.coal])
  (:import [org.bukkit Bukkit Material ChatColor]
           [org.bukkit.event HandlerList]
           [org.bukkit.entity Arrow Player Horse]
           [org.bukkit.inventory ItemStack]
           [org.bukkit.util Vector]))

(defn PlayerToggleSprintEvent [^org.bukkit.event.player.PlayerToggleSprintEvent evt]
  (cloft2.fast-dash/PlayerToggleSprintEvent evt))

(defn PlayerToggleSneakEvent [^org.bukkit.event.player.PlayerToggleSneakEvent evt]
  (cloft2.sneaking-jump/PlayerToggleSneakEvent evt (-> evt .getPlayer)))

(defn PlayerMoveEvent [^org.bukkit.event.player.PlayerMoveEvent evt]
  (cloft2.sneaking-jump/PlayerMoveEvent evt (-> evt .getPlayer)))

(defn EntityDamageEvent [^org.bukkit.event.entity.EntityDamageEvent evt]
  (when (= org.bukkit.event.entity.EntityDamageEvent (.getClass evt))
    (cloft2.sneaking-jump/EntityDamageEvent evt (-> evt .getEntity))
    (cloft2.safe-grass/EntityDamageEvent evt (-> evt .getEntity))))

(defn EntityDeathEvent [^org.bukkit.event.entity.EntityDeathEvent evt]
  (if (= org.bukkit.event.entity.PlayerDeathEvent (.getClass evt))
    (let [player (-> evt .getEntity)]
      (l/post-lingr (-> evt .getDeathMessage)))
    (let [entity (-> evt .getEntity)]
      (when-let [killer-player (-> entity .getKiller)]
        (let [name (->> entity class str (re-find #"Craft([^)]*)") last)
              msg (<< "~{ChatColor/AQUA}~(.getName killer-player)~{ChatColor/RESET} killed a ~{name}")]
          (l/post-lingr msg)
          (Bukkit/broadcastMessage msg))))))

(defn AsyncPlayerChatEvent [^org.bukkit.event.player.AsyncPlayerChatEvent evt]
  (let [player (-> evt .getPlayer)
        msg (-> evt .getMessage
              (s/replace #"benri" "便利")
              (s/replace #"[fh]u[bv]en" "不便")
              (s/replace #"wa-i" "わーい[^。^]")
              (s/replace #"kawaisou" "かわいそう。・°°・(((p(≧□≦)q)))・°°・。ｳﾜｰﾝ!!")
              (s/replace #"dropper|ドロッパ" "泥(・ω・)ﾉ■ ｯﾊﾟ")
              (s/replace #"hopper|ホッパ" "穂(・ω・)ﾉ■ ｯﾊﾟ")
              (s/replace #"\bkiken" "危険")
              (s/replace #"\banzen" "安全")
              (s/replace #"wkwk" "((o(´∀｀)o))ﾜｸﾜｸ")
              (s/replace #"unko" (<< "unko大量生産!ブリブリo(-\"-;)o~{ChatColor/DARK_RED}⌒ξ~ξ~ξ~ξ~ξ~ξ~ξ~ξ~~{ChatColor/RESET}"))
              (s/replace #"dks" "溺((o(´o｀)o))死")
              (s/replace #"tkm" (<< "~{ChatColor/MAGIC}匠~{ChatColor/RESET}"))
              (s/replace #"^!\?$", "!? な、なんだってーΩ ΩΩ")
              (s/replace #"^!list$" (<< "!list\n~{ChatColor/YELLOW}~(s/join \" \" (map #(.getName %) (Bukkit/getOnlinePlayers)))")))
        postmsg (<< "<~(-> player .getName)> ~{msg}")]
    (.setFormat evt (<< "~{ChatColor/AQUA}<%1$s>~{ChatColor/RESET} %2$s")); by default "<%1$s> %2$s"
    (.setMessage evt msg)
    (l/post-lingr postmsg)))

(defn PlayerLoginEvent [^org.bukkit.event.player.PlayerLoginEvent evt]
  (let [player (-> evt .getPlayer)
        msg (<< "~(-> player .getName) logged in.")]
    (l/post-lingr msg ['login-logout (-> player .getName)])
    (later 0
      (.sendMessage player "Welcome to cloft2!")
      (.sendMessage player "Dynmap http://mck.supermomonga.com:8123/"))))

(def hoes #{Material/WOOD_HOE Material/IRON_HOE Material/GOLD_HOE Material/DIAMOND_HOE})

(defn PlayerInteractEvent [^org.bukkit.event.player.PlayerInteractEvent evt]
  (let [player (.getPlayer evt)]
    (condp = (.getAction evt)
      org.bukkit.event.block.Action/LEFT_CLICK_AIR
      (when (= "槍" (-> player .getItemInHand .getItemMeta .getDisplayName))
        (let [arrow (.launchProjectile player Arrow)]
          (later 0
            (.setVelocity arrow (.multiply (.getVelocity arrow) 2)))
          (later (sec 0.15)
            (when (.isValid arrow)
              (.remove arrow)))))
      org.bukkit.event.block.Action/RIGHT_CLICK_AIR
      (when (and (= Material/BOW (-> player .getItemInHand .getType))
                 (instance? Horse (-> player .getVehicle)))
        (let [horse (-> player .getVehicle)
              vel (.getVelocity horse)]
          #_(later 0
            (l/set-velocity horse
                            (* 10 (.getX vel))
                            (inc (.getY vel))
                            (* 10 (.getZ vel))))))
      org.bukkit.event.block.Action/RIGHT_CLICK_BLOCK
      (let [block (.getClickedBlock evt)]
        (when (and (= Material/LONG_GRASS (.getType block))
                   (contains? hoes (-> player .getItemInHand .getType)))
          (.breakNaturally block (-> player .getItemInHand))))
      nil)))

(defn PlayerQuitEvent [^org.bukkit.event.player.PlayerQuitEvent evt]
  (let [msg (<< "~(-> evt .getPlayer .getName) logged out.")]
    (l/post-lingr msg ['login-logout (-> evt .getPlayer .getName)])))

(defn BlockDamageEvent [evt]
  (let [block (-> evt .getBlock)
        player (-> evt .getPlayer)]
    (let [spade (-> player .getItemInHand)]
      (when (and (= Material/WOOD_SPADE (-> player .getItemInHand .getType))
                 (#{Material/GRASS Material/DIRT Material/GRAVEL} (-> block .getType)))
        (doseq [x (range -1 2)
                y (range -1 2)
                z (range -1 2)
                :let [b (-> (doto (-> block .getLocation)
                              (.add x y z))
                          .getBlock)]
                :when (#{Material/GRASS Material/DIRT Material/GRAVEL} (-> b .getType))]
          (.breakNaturally b spade))
        (-> spade (.setDurability (inc (.getDurability spade))))
        (when (< (.getMaxDurability (Material/WOOD_SPADE))
                 (.getDurability spade))
          (let [new-value (dec (.getAmount spade))]
            (if (= new-value 0)
              (.setItemInHand player nil)
              (.setAmount spade new-value))))))))

(defn BlockBreakEvent [^org.bukkit.event.block.BlockBreakEvent evt]
  ; weird bug
  (when (instance? org.bukkit.event.block.BlockBreakEvent evt)
    (let [block (-> evt .getBlock)]
      (cloft2.kickory/BlockBreakEvent evt block)
      (cloft2.coal/BlockBreakEvent evt block))))

(defn BlockPhysicsEvent [^org.bukkit.event.block.BlockPhysicsEvent evt]
  (let [block (-> evt .getBlock)]
    (cloft2.kickory/BlockPhysicsEvent evt block)))

(defn EntityCombustEvent [evt]
  #_(let [entity (.getEntity evt)]
    (when (and (instance? org.bukkit.entity.Item entity)
               (= Material/COAL_ORE (-> entity .getItemStack .getType)))
      (.setDuration evt 0)
      (.setCancelled evt true))))

(defn EntityChangeBlockEvent [^org.bukkit.event.entity.EntityChangeBlockEvent evt]
  (let [entity (.getEntity evt)]
    (cloft2.kickory/EntityChangeBlockEvent evt entity)
    (cloft2.coal/EntityChangeBlockEvent evt entity)))

(defn FurnaceBurnEvent [^org.bukkit.event.inventory.FurnaceBurnEvent evt]
  (let [block (.getBlock evt)]
    (let [fuel (.getFuel evt)]
      (cond
        ; when it's charcoal
        (and (= Material/COAL (.getType fuel))
             (= 1 (-> fuel .getData .getData)))
        (.setBurnTime evt (sec 20)) ; default is 80
        ; when it's coal
        (and (= Material/COAL (.getType fuel))
             (= 0 (-> fuel .getData .getData)))
        (.setBurnTime evt (sec 320)) ; default is 80
        ; when it's coal block
        (= Material/COAL_BLOCK (.getType fuel))
        (.setBurnTime evt (sec 1600))  ; default is 800
        :else nil))))

(defn ProjectileLaunchEvent [^org.bukkit.event.entity.ProjectileLaunchEvent evt]
  (let [entity (.getEntity evt)
        shooter (.getShooter entity)]
    (when (and (instance? Arrow entity)
               (= Horse (.getVehicle shooter)))
      (.teleport entity (l/add-loc (.getEyeLocation shooter) 0 1.5 0)))))

(def table {org.bukkit.event.player.AsyncPlayerChatEvent
            AsyncPlayerChatEvent
            org.bukkit.event.player.PlayerLoginEvent
            PlayerLoginEvent
            org.bukkit.event.player.PlayerQuitEvent
            PlayerQuitEvent
            org.bukkit.event.player.PlayerInteractEvent
            PlayerInteractEvent
            org.bukkit.event.player.PlayerToggleSprintEvent
            PlayerToggleSprintEvent
            org.bukkit.event.player.PlayerToggleSneakEvent
            PlayerToggleSneakEvent
            org.bukkit.event.player.PlayerMoveEvent
            PlayerMoveEvent
            org.bukkit.event.entity.EntityDamageEvent
            EntityDamageEvent
            org.bukkit.event.block.BlockDamageEvent
            BlockDamageEvent
            org.bukkit.event.block.BlockBreakEvent
            BlockBreakEvent
            org.bukkit.event.entity.EntityDeathEvent
            EntityDeathEvent
            org.bukkit.event.block.BlockPhysicsEvent
            BlockPhysicsEvent
            org.bukkit.event.entity.EntityCombustEvent
            EntityCombustEvent
            org.bukkit.event.entity.EntityChangeBlockEvent
            EntityChangeBlockEvent
            org.bukkit.event.inventory.FurnaceBurnEvent
            FurnaceBurnEvent
            org.bukkit.event.entity.ProjectileLaunchEvent
            ProjectileLaunchEvent})

(Bukkit/resetRecipes)
(let [recipe (org.bukkit.inventory.FurnaceRecipe.
               (ItemStack. Material/BREAD 1) Material/WHEAT)]
  (Bukkit/addRecipe recipe))

(let [plugin-manager (Bukkit/getPluginManager)
      plugin (-> plugin-manager (.getPlugin "cloft2"))
      ujm (Bukkit/getPlayer "ujm")]
  (HandlerList/unregisterAll plugin)
  (doseq [[event-class event-f] table
          :let [executer
                (reify org.bukkit.plugin.EventExecutor
                  (execute [_ _ evt]
                    (event-f evt)))]]
    (.registerEvent
      plugin-manager
      event-class
      plugin
      org.bukkit.event.EventPriority/NORMAL
      executer
      plugin))
  #_(let [horse (.getVehicle ujm)]
    (l/set-velocity horse -10 0 0))
  #_(let [horse (.getVehicle ujm)]
    (.eject horse)
    (.teleport ujm (doto (.getLocation ujm)
                       (.setPitch 0)
                       (.setYaw 0)))
    (.setPassenger horse ujm))
  #_(l/rename (.getItemInHand ujm) "槍")

  #_(let [horse (l/spawn (.getLocation ujm) org.bukkit.entity.Horse)]
    (later 0
      (.setTamed horse true)
      (.setOwner horse ujm)))
  #_(prn (some-> ujm .getItemInHand (.setAmount 0))))

[(.getName *ns*) 'SUCCESSFULLY-COMPLETED]
; vim: set lispwords+=later :
