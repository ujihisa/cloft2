(ns cloft2.core
  (:use [clojure.core.strint :only (<<)]
        [cloft2.lib :only (later sec)])
  (:require [clj-http.client]
            [clojure.string :as s]
            [cloft2.fast-dash]
            [cloft2.sneaking-jump]
            [cloft2.safe-grass]
            [cloft2.kickory])
  (:import [org.bukkit Bukkit Material]
           [org.bukkit.event HandlerList]
           [org.bukkit.inventory ItemStack]))

(let [recent-msgs (atom [])]
  (defn post-lingr [msg & [msgtype]]
    #_(prn '= msgtype
    (-> @recent-msgs first first)
    (-> @recent-msgs second first))
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
          :text (str msg)
          :bot_verifier "bb5060f31bc6e89018c55ac72d39d5ca6aca75c9"}}))))

(defn PlayerToggleSprintEvent [evt]
  (cloft2.fast-dash/PlayerToggleSprintEvent evt))

(defn PlayerToggleSneakEvent [evt]
  (cloft2.sneaking-jump/PlayerToggleSneakEvent evt (-> evt .getPlayer)))

(defn PlayerMoveEvent [evt]
  (cloft2.sneaking-jump/PlayerMoveEvent evt (-> evt .getPlayer)))

(defn EntityDamageEvent [evt]
  (when (= org.bukkit.event.entity.EntityDamageEvent (.getClass evt))
    (cloft2.safe-grass/EntityDamageEvent evt (-> evt .getEntity))))

(defn AsyncPlayerChatEvent [evt]
  (let [player (-> evt .getPlayer)
        msg (-> evt .getMessage
              (s/replace #"benri" "便利")
              (s/replace #"[fh]u[bv]en" "不便")
              (s/replace #"wa-i" "わーい[^。^]")
              (s/replace #"dropper|ドロッパ" "泥(・ω・)ﾉ■ ｯﾊﾟ")
              (s/replace #"hopper|ホッパ" "穂(・ω・)ﾉ■ ｯﾊﾟ")
              (s/replace #"\bkiken" "危険")
              (s/replace #"\banzen" "安全")
              (s/replace #"wkwk" "((o(´∀｀)o))ﾜｸﾜｸ")
              (s/replace #"unko" "unko大量生産!ブリブリo(-\"-;)o⌒ξ~ξ~ξ~ξ~ξ~ξ~ξ~ξ~")
              (s/replace #"dks" "溺((o(´o｀)o))死")
              (s/replace #"tkm" "匠")
              (s/replace #"^!\?$", "!? な、なんだってーΩ ΩΩ")
              (s/replace #"^!list$" (<< "!list\n~(s/join \" \" (map #(.getName %) (Bukkit/getOnlinePlayers)))")))
        postmsg (<< "<~(-> player .getName)> ~{msg}")]
    (.setMessage evt msg)
    (post-lingr postmsg)))

(defn PlayerLoginEvent [evt]
  (let [player (-> evt .getPlayer)
        msg (<< "~(-> player .getName) logged in.")]
    (post-lingr msg ['login-logout (-> player .getName)])
    (later 0
      (.sendMessage player "Welcome to cloft2!")
      (.sendMessage player "Dynmap http://mck.supermomonga.com:8123/"))))

(defn PlayerInteractEvent [evt]
  #_(condp = (.getAction evt)
    org.bukkit.event.block.Action/LEFT_CLICK_AIR
    #_(let [msg (<< "~(-> evt .getPlayer .getName) clicked air at ~(-> evt .getPlayer .getLocation)")]
    (prn msg)
    (prn (post-lingr msg)))
    (prn :else (.getAction evt))))

(defn PlayerQuitEvent [evt]
  (let [msg (<< "~(-> evt .getPlayer .getName) logged out.")]
    (post-lingr msg ['login-logout (-> evt .getPlayer .getName)])))

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
  (cloft2.kickory/BlockBreakEvent evt (-> evt .getBlock)))

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
            BlockBreakEvent})

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
