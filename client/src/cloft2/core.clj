(ns cloft2.core
  (:require [clojure.string :as s]
            [clojure.tools.nrepl :as nrepl]))

(defn remote-eval* [nrepl-client expr]
  )

(defmacro remote-eval [nrepl-client & exprs]
  `(remote-eval* ~nrepl-client '(do ~@exprs)))

(defn -main [& args]
  (with-open [nrepl-conn (nrepl/connect :host "0.0.0.0" :port 7888)]
    (let [nrepl-client (nrepl/client nrepl-conn 2000)]
      (doseq [file-path ["/home/ujihisa/git/cloft2/client/src/cloft2/lib.clj"
                         "/home/ujihisa/git/cloft2/client/src/cloft2/fast_dash.clj"
                         "/home/ujihisa/git/cloft2/client/src/cloft2/sneaking_jump.clj"
                         "/home/ujihisa/git/cloft2/client/src/cloft2/safe_grass.clj"
                         "/home/ujihisa/git/cloft2/client/src/cloft2/app.clj"]
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
          #_(finally (.close nrepl-conn))))))
  (System/exit 0))
; vim: set lispwords+=remote-eval,later :
