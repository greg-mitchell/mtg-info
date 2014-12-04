(ns mtg-info.resources.placings
  (:import (java.net URL))
  (:require [liberator.core :as liber]
            [liberator.representation :refer :all]
            [hiccup.core :as h]
            [mtg-info.resources.util :as util]
            [clojure.string :as s]))

;; we hold a entries in this ref
(defonce entries (ref {}))

(defrecord Placing [name deck date event location place id])

(defn parse-line [line]
  (->> (s/split line #"\t")
       ;; TODO: parse date
       (apply ->Placing)))

(defn split-and-parse-body [^String body]
  (->> (s/split-lines body)
       (map parse-line)))

;; a helper to create a absolute url for the entry with the given id
(defn build-entry-url [request id]
  (URL. (format "%s://%s:%s%s/%s"
                (name (:scheme request))
                (:server-name request)
                (:server-port request)
                (:uri request)
                (str id))))

(defn build-placings-html
  [ctx placings-seq]
  (h/html
    [:div#placings.resourceList
     [:h1 "Placings"]
     [:hr]
     [:table {:style "border: 0; width: 90%"}
      [:thead
       [:tr (util/map-tag :th {:align "left"}
                          ["ID" "Player Name" "Deck Name" "Event" "Location" "Finish"])]]
      (into [:tbody]
            (for [placing placings-seq]
              [:tr
               [:td [:a {:href (build-entry-url (:request ctx) (:id placing))} (:id placing)]]
               (util/map-tag :td
                             (map #(% placing)
                                  [:name :deck :event :location :place]))]))]]))

(defn find-entities
  [ctx entries]
  (let [params (get-in ctx [:request :params])]
    (->> entries
         (map second)
         (filter (fn [entry]
                   (every? identity
                           (map (fn [[param-k param-v]]
                                  (= param-v (param-k entry)))
                                params)))))))

;; create and list entries
(liber/defresource placings-list
             :available-media-types ["text/html"]
             :allowed-methods [:get :post :put]
             :known-content-type? #(util/check-content-type % ["text/plain"])
             :malformed? #(util/parse-body % ::data split-and-parse-body)
             :post! #(let [entry (first (::data %))
                           id (:id entry)]
                      (dosync
                        (alter entries assoc id entry))
                      {::id id})
             :put! #(let [data (::data %)]
                     (dosync
                       (doall
                         (for [entry data
                               :let [id (:id entry)]]
                           (alter entries assoc id entry)))))
             :post-redirect? true
             :location #(when-let [id (get % ::id)]
                         (build-entry-url (get % :request) id))
             :handle-ok (fn [ctx]
                          (let [returned-entities (find-entities ctx @entries)]
                            (build-placings-html ctx returned-entities))))

(liber/defresource placing-entry [id]
             :allowed-methods [:get :put :delete]
             :known-content-type? #(util/check-content-type % ["application/json"])
             :exists? (fn [_]
                        (let [e (get @entries id)]
                          (if-not (nil? e)
                            {::entry e})))
             :existed? (fn [_] (nil? (get @entries id ::sentinel)))
             :available-media-types ["text/html" "application/json"]
             :handle-ok #(into {} (::entry %))
             :delete! (fn [_] (dosync (alter entries assoc id nil)))
             :malformed? #(util/parse-body % ::data parse-line)
             :can-put-to-missing? false
             :put! #(dosync (alter entries assoc id (::data %)))
             :new? (fn [_] (nil? (get @entries id ::sentinel))))
