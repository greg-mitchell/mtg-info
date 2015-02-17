(ns mtg-info.resources.audit
  (:import [java.util UUID])
  (:require [liberator.core :as liber]
            [liberator.representation :refer :all]
            [hiccup.core :as h]
            [mtg-info.resources.util :as util]
            [mtg-info.layout.html :as layout]
            [clojure.string :as s]
            [clj-time.core :as time]))

(defonce ^:private audit (ref {}))

(defn create-audit
  "Create an audit from a list of attempted resource creations. The invalid
   resources should be represented as a map with the :error key. This function
   must be invoked in a dosync block."
  [resource-seq]
  (let [total-count (count resource-seq)
        errors (filter #(and (map? %) (contains? % :error)) resource-seq)
        successful-ids (remove nil? (map :id resource-seq))
        error-count (count errors)
        success-count (- total-count error-count)
        audit-id (str (UUID/randomUUID))]
    (alter audit assoc audit-id {:success-count success-count
                                 :error-count   error-count
                                 :total         total-count
                                 :successes     successful-ids
                                 :errors        errors
                                 :date          (time/now)})
    audit-id))

(defn- build-audit-html
  [ctx audit-map]
  (let [sorted-audits (->> audit-map (sort-by #(:date (val %))) (reverse))]
    [:div {:id "audits" :class "content"}
     [:h1 "Audits"]
     [:hr]
     [:table {:style "border: 0; width: 90%"}
      [:thead
       [:tr (util/map-tag :th {:align "left"} ["ID" "Date" "Errors" "Total"])]]
      (into [:tbody]
            (for [[id audit] sorted-audits]
              [:tr
               [:td [:a {:href (util/build-entry-url (:request ctx) id)} id]]
               (util/map-tag :td (map #(% audit) [:date :error-count :total]))]))]]))

(liber/defresource audits-list
  :allowed-methods [:get]
  :available-media-types ["text/html"]
  :handle-ok (fn [ctx]
               (layout/application "Audits" (layout/navbar "/audits") (build-audit-html ctx @audit))))

(liber/defresource audit-entry [id]
  :allowed-methods [:get]
  :available-media-types ["text/html" "application/json"]
  :exists? (fn [_ctx]
             (when-let [e (get @audit id)]
               {::entry e}))
  :handle-ok #(::entry %))