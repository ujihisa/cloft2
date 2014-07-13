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
          (:import [org.bukkit Bukkit]
                   [org.bukkit.event HandlerList]))
        (defn player-login-event [evt]
          (prn 'player (-> evt .getPlayer .getName) 'logged 'in))

        (let [plugin-manager (Bukkit/getPluginManager)
              plugin (-> plugin-manager (.getPlugin "cloft2"))
              ujm (Bukkit/getOfflinePlayer "ujm")]
          (prn (HandlerList/unregisterAll plugin))
          (let [executer
                (reify org.bukkit.plugin.EventExecutor
                  (execute [_ _ evt]
                    (player-login-event evt)))]
            (.registerEvent
              plugin-manager
              org.bukkit.event.player.PlayerLoginEvent
              plugin
              org.bukkit.event.EventPriority/NORMAL
              executer
              plugin))
          ujm)
        'done)))
  (System/exit 0))
; vim: set lispwords+=remote-eval :
