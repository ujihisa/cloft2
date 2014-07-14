(ns cloft2.core
  (:require [clojure.string :as s]
            [clojure.tools.nrepl :as repl]))

(defn remote-eval* [nrepl-client expr]
  (try
    (doseq [resp (repl/message nrepl-client {:op "eval" :code (str expr)})]
      (condp #(get %2 %1) resp
        :out (println (:out resp))
        :value (println (str "(RETURN VALUE) " (:value resp)))
        :ex (println (str "!!!" (:ex resp) "!!!"))
        :err (println (str "!!!" (:err resp) "!!!"))
        (prn resp)))
    (catch Exception e (-> e .printStackTrace))
    #_(finally (.close nrepl-conn))))

(defmacro remote-eval [nrepl-client & exprs]
  `(remote-eval* ~nrepl-client '(do ~@exprs)))

(defn -main [& args]
  (with-open [nrepl-conn (repl/connect :host "mck.supermomonga.com" :port 7888)]
    (let [nrepl-client (repl/client nrepl-conn 2000)]
      (remote-eval nrepl-client
        (ns cloft2.core
          (:use [clojure.core.strint :only (<<)])
          (:require [clj-http.client]
                    [clojure.string :as s])
          (:import [org.bukkit Bukkit]
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

        (let [plugin (-> (Bukkit/getPluginManager) (.getPlugin "cloft2"))]
          (defn later* [tick f]
            (.scheduleSyncDelayedTask
              (org.bukkit.Bukkit/getScheduler) plugin f tick)))
        (defmacro later [tick & exps]
          `(later* ~tick (fn [] ~@exps)))

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
              #_(.sendMessage player "Minecraft→Lingr通知が行われるが、Lingr→Minecraft通知は未実装なので要注意"))))
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
                    PlayerInteractEvent})

        (let [plugin-manager (Bukkit/getPluginManager)
              plugin (-> plugin-manager (.getPlugin "cloft2"))
              ujm (Bukkit/getOfflinePlayer "ujm")]
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
        'done)))
  (System/exit 0))
; vim: set lispwords+=remote-eval,later :
