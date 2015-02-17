(ns mtg-info.resources.placings
  (:require [liberator.core :as liber]
            [liberator.representation :refer :all]
            [hiccup.core :as h]
            [mtg-info.resources.util :as util]
            [mtg-info.layout.html :as layout]
            [mtg-info.resources.audit :as audit]
            [clojure.string :as s]))

;; we hold a entries in this ref
(defonce entries (ref {}))
(defonce indices (ref {}))

(defrecord Placing [name deck date location place id event format])

(defn- cleanup-line
  [l]
  (let [place (get l 4)
        [_ place-digits] (re-matches #"(\d+)\D*" place)
        num-place (Integer/parseInt place-digits)]
    (assoc l 4 num-place)))

(defn- validate-placing
  [placing]
  (let [{:keys [name deck date location place id event format]} placing]
    (->> {}
      (#(if (re-matches #"[\p{Alnum} ]+" name)
         %
         (assoc % :name (str "Name has invalid characters: " name))))
      (#(if (re-matches #"[\p{Alnum} ]+" deck)
         %
         (assoc % :deck (str "Deck name has invalid characters: " deck))))
      (#(if (empty? %) placing {:line placing :error %})))))

(defn parse-line [line]
  (if-not (< (count line) 1000)
    {:line line
     :error "Line is too long. It must be less than 1000 characters."}
    (let [line-fields (s/split line #"\t")]
      (if-not (= 8 (count line-fields))
        {:placing line-fields
         :error   "Incorrect number of fields in line."}
        (validate-placing (apply ->Placing (cleanup-line line-fields)))))))

(defn split-and-parse-body [^String body]
  (->> (s/split-lines body)
       (map parse-line)))

(defn build-placings-html
  [ctx placings-seq]
  [:div {:id "placings" :class "content"}
   [:h1 "Placings"]
   [:hr]
   [:table {:style "border: 0; width: 90%"}
    [:thead
     [:tr (util/map-tag :th {:align "left"}
                        ["ID" "Player Name" "Deck Name" "Event" "Location" "Date" "Finish"])]]
    (into [:tbody]
          (for [placing placings-seq]
            [:tr
             [:td [:a {:href (util/build-entry-url (:request ctx) (:id placing))} (:id placing)]]
             (util/map-tag :td
                           (map #(% placing)
                                [:name :deck :event :location :date :place]))]))]])

(defn- entry-matches?
  [params match-fn entry]
  (->> (select-keys params (keys entry))
       (remove #(s/blank? (second %)))
       (map (fn [[param-k param-v]] (match-fn param-v (param-k entry))))
       (every? identity)))

(defn- get-match-fn
  [params]
  (let [match-by (:matchby params)
        partial-match (fn [term entry]
                        (when entry (.contains (.toLowerCase entry) (.toLowerCase term))))]
    (cond
      (= "exact" match-by) =
      (= "partial" match-by) partial-match
      :else partial-match)))

(defn find-entities
  [ctx entries]
  (let [params (get-in ctx [:request :params])
        sort-key (or (-> params :sortby keyword) :date)
        match-fn (get-match-fn params)
        maybe-reverse (if (= :date sort-key) reverse identity)]
    (->> entries
         (map second)
         (filter (partial entry-matches? params match-fn))
         (sort-by sort-key)
         (maybe-reverse))))

(defn index-starts-with
  [field req]
  (let [field-kw (keyword field)
        term (-> req :params :term)
        term (when term (.toLowerCase term))
        matches? (fn [[k _v]] (.startsWith k term))
        index (@indices field-kw)]
    (when (and term index)
      (->> index
           (filter matches?)
           (map second)))))

(defn- update-indices
  [indices entry]
  (doseq [[field value] entry]
    (when (instance? String value)
      (alter indices (fn [ind-map]
                       (assoc-in ind-map [field (.toLowerCase value)] value))))))

(defn build-scg-link
  [id]
  (let [link (str "http://sales.starcitygames.com/deckdatabase/displaydeck.php?DeckID=" id)]
    (h/html [:a {:href link} link])))

(defn- parse-and-validate-data
  [ctx]
  (let [content-type (get-in ctx [:request :content-type])
        params (get-in ctx [:request :params])]
    (condp = content-type
      "application/x-www-form-urlencoded"
      [false {::data (list (validate-placing (map->Placing params)))}]
      ;; default
      (util/parse-body ctx ::data split-and-parse-body))))

;; create and list entries
(liber/defresource placings-list
             :available-media-types ["text/html"]
             :allowed-methods [:get :post :put]
             :known-content-type? #(util/check-content-type % ["text/plain" "application/x-www-form-urlencoded"])
             :malformed? parse-and-validate-data
             :post! #(let [data (::data %)
                           entry (first data)
                           id (:id entry)]
                      (dosync
                        (when id
                          (alter entries assoc id entry)
                          (update-indices indices entry))
                        {::audit-id (audit/create-audit data)}))
             :put! #(let [data (::data %)]
                     (dosync
                       (doseq [entry data]
                         (let [id (:id entry)]
                           (alter entries assoc id entry)
                           (update-indices indices entry)))
                       {::audit-id (audit/create-audit data)}))
             :post-redirect? true
             :location #(when-let [id (get % ::audit-id)]
                         (util/build-entry-url (get % :request) id "/audits"))
             :handle-ok (fn [ctx]
                          (let [returned-entities (find-entities ctx @entries)]
                            (layout/application "Placings"
                                                (layout/navbar "/placings")
                                                (build-placings-html ctx returned-entities)))))

(liber/defresource placing-entry [id]
             :allowed-methods [:get :put :delete]
             :known-content-type? #(util/check-content-type % ["application/json"])
             :exists? (fn [_ctx]
                        (when-let [e (get @entries id)]
                          {::entry e}))
             :existed? (fn [_ctx] (nil? (get @entries id ::sentinel)))
             :available-media-types ["text/html" "application/json"]
             :handle-ok #(let [entry (::entry %)]
                          (assoc (into {} entry)
                                 :id (build-scg-link (:id entry))))
             ;; TODO: handle updating indices
             :delete! (fn [_] (dosync (alter entries assoc id nil)))
             :malformed? #(util/parse-body % ::data parse-line)
             :can-put-to-missing? false
             :put! #(dosync (alter entries assoc id (::data %)))
             :new? (fn [_] (nil? (get @entries id ::sentinel))))
