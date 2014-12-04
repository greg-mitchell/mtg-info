(ns mtg-info.server
  (:require [mtg-info.core.handler :as handler]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(defn run-server [port]
  (jetty/run-jetty handler/app {:port port}))

(defn -main [& args]
  (run-server 8080))
