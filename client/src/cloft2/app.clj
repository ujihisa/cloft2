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
           [org.bukkit.inventory ItemStack]
           [org.bukkit.util Vector]))

(defn PlayerToggleSprintEvent [^org.bukkit.event.player.PlayerToggleSprintEvent evt]
  (cloft2.fast-dash/PlayerToggleSprintEvent evt))

(defn PlayerToggleSneakEvent [evt]
  (cloft2.sneaking-jump/PlayerToggleSneakEvent evt (-> evt .getPlayer)))

(defn PlayerMoveEvent [evt]
  (cloft2.sneaking-jump/PlayerMoveEvent evt (-> evt .getPlayer)))

(defn EntityDamageEvent [evt]
  (when (= org.bukkit.event.entity.EntityDamageEvent (.getClass evt))
    (cloft2.safe-grass/EntityDamageEvent evt (-> evt .getEntity))))

(defn EntityDeathEvent [evt]
  (if (= org.bukkit.event.entity.PlayerDeathEvent (.getClass evt))
    (let [player (-> evt .getEntity)]
      (l/post-lingr (-> evt .getDeathMessage)))
    (let [entity (-> evt .getEntity)]
      (when-let [killer-player (-> entity .getKiller)]
        (let [name (.getClass entity)]
          (l/post-lingr (<< "~(.getName killer-player) killed a ~{name})")))))))

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
    (.setFormat evt (<< "~{ChatColor/YELLOW}<%1$s>~{ChatColor/RESET} %2$s")); by default "<%1$s> %2$s"
    (.setMessage evt msg)
    (l/post-lingr postmsg)))

(defn PlayerLoginEvent [^org.bukkit.event.player.PlayerLoginEvent evt]
  (let [player (-> evt .getPlayer)
        msg (<< "~(-> player .getName) logged in.")]
    (l/post-lingr msg ['login-logout (-> player .getName)])
    (later 0
      (.sendMessage player "Welcome to cloft2!")
      (.sendMessage player "Dynmap http://mck.supermomonga.com:8123/"))))

(defn PlayerInteractEvent [^org.bukkit.event.player.PlayerInteractEvent evt]
  #_(condp = (.getAction evt)
    org.bukkit.event.block.Action/LEFT_CLICK_AIR
    #_(let [msg (<< "~(-> evt .getPlayer .getName) clicked air at ~(-> evt .getPlayer .getLocation)")]
    (prn msg)
    (prn (l/post-lingr msg)))
    (prn :else (.getAction evt))))

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

(defn BlockBreakEvent [evt]
  (let [block (-> evt .getBlock)]
    (cloft2.kickory/BlockBreakEvent evt block)
    (cloft2.coal/BlockBreakEvent evt block)))

(defn drop-item [loc itemstack]
  (.dropItemNaturally (.getWorld loc) loc itemstack))

(defn BlockPhysicsEvent [evt]
  (let [block (-> evt .getBlock)]
    (when (and (#{Material/LEAVES Material/LEAVES_2} (-> block .getType))
               (.isEmpty (l/block-below block))
               (every? #(not (#{Material/LOG Material/LOG_2} (.getType %)))
                       (l/neighbours block))
               (not (#{Material/LEAVES Material/LEAVES_2} (.getChangedType evt))))
      #_(.breakNaturally block)
      #_(.setType block Material/FIRE)
      #_(.setData block 0)
      ; (prn 'block block 'species (-> block .getState .getData .getSpecies))
      (let [sapling (ItemStack. Material/SAPLING 1 (short 0) (.getData block))
            stick (ItemStack. Material/STICK 1)
            apple (ItemStack. Material/APPLE 1)
            golden-apple (ItemStack. Material/APPLE 1)
            itemstack (if (= 0 (.getData block))
                        (rand-nth [sapling apple apple golden-apple stick stick stick stick])
                        (rand-nth [sapling sapling stick stick stick stick stick stick]))]
        (drop-item (-> block .getLocation) itemstack))
      (let [f (l/fall block) #_(.spawnEntity (-> block .getLocation .getWorld) (.getLocation block) org.bukkit.entity.EntityType/BOAT)]
        (later 0
          (.setVelocity f (Vector. (rand-nth [0.5 0.0 -0.5])
                                   (rand-nth [0.5 1.0 1.5])
                                   (rand-nth [0.5 0.0 -0.5]))))))))

(defn EntityCombustEvent [evt]
  #_(let [entity (.getEntity evt)]
    (when (and (instance? org.bukkit.entity.Item entity)
               (= Material/COAL_ORE (-> entity .getItemStack .getType)))
      (.setDuration evt 0)
      (.setCancelled evt true))))

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
            EntityCombustEvent})

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
  #_(prn (some-> ujm .getItemInHand (.setAmount 0))))

[(.getName *ns*) 'SUCCESSFULLY-COMPLETED]
; vim: set lispwords+=later :
