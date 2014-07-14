(ns cloft2.core
  (:use [clojure.core.strint :only (<<)])
  (:require [clj-http.client]
            [clojure.string :as s])
  (:import [org.bukkit Bukkit Material]
           [org.bukkit.event HandlerList]))
(let [recent-msgs (atom [])]
  (defn post [msg & [msgtype]]
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

(defn sec [n]
  (int (* 20 n)))
(let [plugin (-> (Bukkit/getPluginManager) (.getPlugin "cloft2"))]
  (defn later* [tick f]
    (.scheduleSyncDelayedTask
      (org.bukkit.Bukkit/getScheduler) plugin f tick)))
(defmacro later [tick & exps]
  `(later* ~tick (fn [] ~@exps)))

(def dash-id-table (atom {}))
(defn PlayerToggleSprintEvent [evt]
  (let [player (-> evt .getPlayer)]
    (if (and (.isSprinting evt) (not (.getPassenger player)))
      (if (= Material/SAND (-> player .getLocation .clone (.add 0 -1 0) .getBlock .getType))
        (.setCancelled evt true)
        (let [dash-id (rand)]
          (.setWalkSpeed player 0.4)
          (swap! dash-id-table assoc player dash-id)
          (later (sec 4)
                 (when (= dash-id (@dash-id-table player))
                   #_(helper/smoke-effect (.getLocation player))
                   (.setWalkSpeed player 0.6)))))
      (do
        (.setWalkSpeed player 0.2)
        (swap! dash-id-table assoc player nil)))))

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
    (post postmsg)))
(defn PlayerLoginEvent [evt]
  (let [player (-> evt .getPlayer)
        msg (<< "~(-> player .getName) logged in.")]
    (post msg ['login-logout (-> player .getName)])
    (later 0
           (.sendMessage player "Welcome to cloft2!")
           (.sendMessage player "Dynmap http://mck.supermomonga.com:8123/"))))
(defn PlayerInteractEvent [evt]
  (condp = (.getAction evt)
    org.bukkit.event.block.Action/LEFT_CLICK_AIR
    #_(let [msg (<< "~(-> evt .getPlayer .getName) clicked air at ~(-> evt .getPlayer .getLocation)")]
    (prn msg)
    (prn (post msg)))
    (prn :else (.getAction evt))))
(defn PlayerQuitEvent [evt]
  (let [msg (<< "~(-> evt .getPlayer .getName) logged out.")]
    (post msg ['login-logout (-> evt .getPlayer .getName)])))
(def table {org.bukkit.event.player.AsyncPlayerChatEvent
            AsyncPlayerChatEvent
            org.bukkit.event.player.PlayerLoginEvent
            PlayerLoginEvent
            org.bukkit.event.player.PlayerQuitEvent
            PlayerQuitEvent
            org.bukkit.event.player.PlayerInteractEvent
            PlayerInteractEvent
            org.bukkit.event.player.PlayerToggleSprintEvent
            PlayerToggleSprintEvent})

(let [plugin-manager (Bukkit/getPluginManager)
      plugin (-> plugin-manager (.getPlugin "cloft2"))
      ujm (Bukkit/getPlayer "ujm")]
  (prn (HandlerList/unregisterAll plugin))
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
  ujm)
'SUCCESSFULLY-COMPLETED
; vim: set lispwords+=later :
