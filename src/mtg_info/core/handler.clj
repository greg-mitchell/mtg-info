(ns mtg-info.core.handler
  (:require [mtg-info.core.data :as data]
            [mtg-info.resources.placings :as placings]
            [clojure.java.io :as io]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :as h]
            [mtg-info.layout.html :as layout]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [liberator.dev]
            [cheshire.core :as json]))

(defn jsonify
  [clj-data ]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string clj-data)})

(defroutes app-routes
  (GET "/" [] (layout/application "MTG Infobank" layout/index-html))

  (GET "/indices/placings/:field" [field :as req]
       (jsonify (placings/index-starts-with field req)))

  (ANY "/placings" [] placings/placings-list)

  (ANY "/placings/:id" [id] (placings/placing-entry id))

  (GET "/submit" [] (layout/application "MTG Infobank" layout/submit-html))

  (route/not-found (layout/application "Not Found" layout/not-found-html)))

(def app
  (-> app-routes
      (liberator.dev/wrap-trace )
      (ring.middleware.stacktrace/wrap-stacktrace)
      (wrap-defaults (-> site-defaults
                         (dissoc :session)
                         (assoc-in [:security :anti-forgery] false)))))
