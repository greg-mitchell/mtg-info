(ns mtg-info.core.handler
  (:require [mtg-info.core.data :as data]
            [mtg-info.resources.placings :as placings]
            [mtg-info.resources.audit :as audit]
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
  ;; Browser UI pages
  (GET "/" [] (layout/application "MTG Infobank - Search" (layout/navbar "/") layout/search-html))
  (GET "/submit" [] (layout/application "MTG Infobank - Submit" (layout/navbar "/submit") layout/submit-html))
  (GET "/upload" [] (layout/application "MTG Infobank - Upload" (layout/navbar "/upload") layout/upload-html))

  ;; Resources
  (ANY "/audits" [] audit/audits-list)
  (ANY "/audits/:id" [id] (audit/audit-entry id))
  (ANY "/placings" [] placings/placings-list)
  (ANY "/placings/:id" [id] (placings/placing-entry id))

  ;; To facilitate autocomplete
  (GET "/indices/placings/:field" [field :as req]
       (jsonify (placings/index-starts-with field req)))

  (route/not-found (layout/application "Not Found" (layout/navbar "") layout/not-found-html)))

(def app
  (-> app-routes
      (liberator.dev/wrap-trace )
      (ring.middleware.stacktrace/wrap-stacktrace)
      (wrap-defaults (-> site-defaults
                         (dissoc :session)
                         (assoc-in [:security :anti-forgery] false)))))
