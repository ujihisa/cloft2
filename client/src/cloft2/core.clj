(ns cloft2.core
  (:require [clojure.string :as s]
            [clojure.tools.nrepl :as repl]))

(defn remote-eval* [nrepl-client expr]
  (try
    (-> (repl/message nrepl-client {:op "eval" :code (str expr)})
      first :value println)
    (catch Exception e (-> e .printStackTrace))
    #_(finally (.close nrepl-conn))))

(defmacro remote-eval [nrepl-client & exprs]
  `(remote-eval* ~nrepl-client '(do ~@exprs)))

(defn -main [& args]
  (with-open [nrepl-conn (repl/connect :host "mck.supermomonga.com" :port 7888)]
    (let [nrepl-client (repl/client nrepl-conn 2000)]
      (remote-eval nrepl-client
        (let [ujm (org.bukkit.Bukkit/getOfflinePlayer "ujm")]
          ujm))))
  (System/exit 0))
; vim: set lispwords=remote-eval :
