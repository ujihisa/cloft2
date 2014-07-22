(ns cloft2.core
  (:require [clojure.string :as s]
            [clojure.tools.nrepl :as nrepl]))

#_(defn remote-eval* [nrepl-client expr]
  )

#_(defmacro remote-eval [nrepl-client & exprs]
  `(remote-eval* ~nrepl-client '(do ~@exprs)))

(defn -main [& args]
  (with-open [nrepl-conn (nrepl/connect :host "localhost" :port 7888)]
    (let [nrepl-client (nrepl/client nrepl-conn 2000)]
      (doseq [:let [basedir (System/getenv "PWD")]
              file-path* ["src/cloft2/lib.clj"
                          "src/cloft2/fast_dash.clj"
                          "src/cloft2/kickory.clj"
                          "src/cloft2/coal.clj"
                          "src/cloft2/sneaking_jump.clj"
                          "src/cloft2/safe_grass.clj"
                          "src/cloft2/app.clj"]
              :let [file-path (str basedir "/" file-path*)]
              resp (nrepl/message
                     nrepl-client
                     {:op "load-file" :file (slurp file-path) :file-path file-path})]
        (try
          (condp #(get %2 %1) resp
            :out (println (:out resp))
            :value (println (str "(RETURN VALUE) " (:value resp)))
            :ex (println (str "!!!" (:ex resp) "!!!"))
            :err (println (str "!!!" (s/trim-newline (:err resp)) "!!!"))
            (when-not (= (into #{} (keys resp))
                         #{:id :session :status})
              (prn resp)))
          (catch Exception e (-> e .printStackTrace))
          #_(finally (.close nrepl-conn))))
      (nrepl/message
        nrepl-client
        {:op "load-file"
         :file (str `(do (ns cloft2.dummy (:require [cloft2.lib]))
                           (cloft2.lib/post-lingr (str "Deployed by " ~(System/getenv "USER")))
                           (ort.bukkit.Bukkit/broadcastMessage (str "Deployed by " ~(System/getenv "USER")))))
         :file-path "dummy"})))
  (System/exit 0))
; vim: set lispwords+=remote-eval,later :
