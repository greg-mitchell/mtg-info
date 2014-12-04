(ns mtg-info.core.handler
  (:require [mtg-info.core.data :as data]
            [mtg-info.resources.placings :as placings]
            [clojure.java.io :as io]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :as h]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [liberator.dev]))

(defroutes app-routes
  (GET "/" []
       (h/html [:div
                [:h1 "MTG Infobank"]
                "The search interface is still in progress. In the meantime,
                 search manually at "
                [:a {:href "/placings"} "Placings"]
                [:br]
                "You can query the enties by using HTTP query syntax (?key=val).
                 Keys are id, name, deck, event, location, finish. Vals must be
                 url-encoded."
                [:br]
                "E.g. "
                [:a {:href "/placings?name=Cedric%20Phillips"} "/placings?name=Cedric%20Phillips"]]))

  (ANY "/placings" [] placings/placings-list)

  (ANY "/placings/:id" [id] (placings/placing-entry id))

  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (liberator.dev/wrap-trace )
      (ring.middleware.stacktrace/wrap-stacktrace)
      (wrap-defaults (-> site-defaults
                         (dissoc :session)
                         (assoc-in [:security :anti-forgery] false)))))
