(ns mtg-info.layout.html
  (:require [hiccup.page :as hp]
            [hiccup.element :as he]
            [hiccup.form :as hf]))

;; TODO: fix active
(defn navbar
  [active-link]
  (let [nav-links [["/" "Search"] ["/submit" "Submit Data"] ["/upload" "Upload Data"]]]
    [:nav {:class "navbar navbar-default navbar-fixed-top" :role "navigation"}
     [:div {:class "container"}
      [:div {:class "navbar-header"}
       [:button {:type        "button" :class "navbar-toggle collapsed" :data-toggle "collapse"
                 :data-target "#navbar"}
        [:span {:class "sr-only"} "Toggle navigation"]
        [:span {:class "icon-bar"}]]
       [:a {:class "navbar-brand" :href "/"} "MTG Infobank"]]
      [:div {:id "navbar" :class "navbar-collapse collapse"}
       (into [:ul {:class "nav navbar-nav"}]
             (for [[link label] nav-links]
               (if (= link active-link)
                 [:li {:class "active"} (he/link-to link label)]
                 [:li (he/link-to link label)])))]]]))

(defn application
  [title navbar & content]
  (hp/html5 {:ng-app "myApp" :lang "en"}
            [:head
             [:title title]
             (hp/include-css "//code.jquery.com/ui/1.11.2/themes/smoothness/jquery-ui.css"
                             "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap.min.css"
                             "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap-theme.min.css"
                             "/css/style.css")
             (hp/include-js "https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js")
             (hp/include-js "//code.jquery.com/ui/1.11.2/jquery-ui.js")
             (hp/include-js "/js/script.js")
             [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]]
            [:body
             navbar
             [:div {:class "container-fluid"} content]]))

(def not-found-html
  [:div {:class "content"}
   [:h1 {:class "info-worning"} "Page Not Found"]
   [:p "There's no requested page. "]
   (he/link-to {:class "btn btn-primary"} "/" "Take me to Home")])

(defn- sortby-radio [[label value checked]]
  [:label {:class "radio inline"}
   (hf/radio-button "sortby" checked value)
   label])

(defn- placings-form-fields [& extra-fields]
  (conj (into [[:div {:class "form-group"}
                (hf/label {:class "control-label"} "name" "Player Name")
                (hf/text-field {:class "form-control"} "name")]
               [:div {:class "form-group"}
                (hf/label {:class "control-label"} "deck" "Deck Name")
                (hf/text-field {:class "form-control"} "deck")]
               [:div {:class "form-group"}
                (hf/label {:class "control-label"} "event" "Event")
                (hf/text-field {:class "form-control"} "event")]
               [:div {:class "form-group"}
                (hf/label {:class "control-label"} "location" "Location")
                (hf/text-field {:class "form-control"} "location")]
               [:div {:class "form-group"}
                (hf/label {:class "control-label"} "date" "Date")
                (hf/text-field {:class "form-control"} "date")]
               [:div {:class "form-group"}
                (hf/label {:class "control-label"} "format" "Format")
                (hf/text-field {:class "form-control"} "format")]
               [:div {:class "form-group"}
                (hf/label {:class "control-label"} "place" "Finish")
                (hf/text-field {:class "form-control"} "place")]]
              extra-fields)
        [:br]
        [:div {:class "btn-group" :role "group"}
         (hf/submit-button {:class "btn btn-default"} "Submit")
         (hf/reset-button {:class "btn btn-default"} "Clear")]))

(def search-html
  [:div {:class "content"}
   [:h1 "Search"]
   [:p "Search for player results from the SCG circuit"]
   [:div {:class "well"}
    (into (hf/form-to {:class "form" :role "form"} [:get "/placings"])
          [[:div {:class "form-group"}
            (hf/label {:class "control-label"} "name" "Player Name")
            (hf/text-field {:class "form-control"} "name")]
           [:div {:class "form-group"}
            (hf/label {:class "control-label"} "deck" "Deck Name")
            (hf/text-field {:class "form-control"} "deck")]
           [:div {:class "form-group"}
            (hf/label {:class "control-label"} "event" "Event")
            (hf/text-field {:class "form-control"} "event")]
           [:div {:class "form-group"}
            (hf/label {:class "control-label"} "location" "Location")
            (hf/text-field {:class "form-control"} "location")]
           [:div {:class "form-group"}
            (hf/label {:class "control-label"} "date" "Date")
            (hf/text-field {:class "form-control"} "date")]
           [:div {:class "form-group"}
            (hf/label {:class "control-label"} "format" "Format")
            (hf/text-field {:class "form-control"} "format")]
           [:div {:class "form-group"}
            (hf/label {:class "control-label"} "place" "Finish")
            (hf/text-field {:class "form-control"} "place")]
           [:div {:class "form-group"}
            (hf/label {:class "control-label"} "sortby" "Sort-by")
            (reduce conj [:div {:class "radio"}] (map sortby-radio [["Date" "date" true] ["Place" "place" false] ["Name" "name" false]]))]
           [:br]
           [:div {:class "btn-group" :role "group"}
            (hf/submit-button {:class "btn btn-default"} "Search")
            (hf/reset-button {:class "btn btn-default"} "Clear")]])]])

(def submit-html
  [:div {:class "content"}
   [:h1 "Submit Data"]
   [:p "Add new player data to the database."]
   [:div {:class "well"}
    (into (hf/form-to {:class "form" :role "form"} [:post "/placings"])
          [[:div {:class "form-group"}
            (hf/label {:class "control-label"} "id" "SCG ID")
            (hf/text-field {:class "form-control"} "id")]
           [:div {:class "form-group"}
            (hf/label {:class "control-label"} "name" "Player Name")
            (hf/text-field {:class "form-control"} "name")]
           [:div {:class "form-group"}
            (hf/label {:class "control-label"} "deck" "Deck Name")
            (hf/text-field {:class "form-control"} "deck")]
           [:div {:class "form-group"}
            (hf/label {:class "control-label"} "event" "Event")
            (hf/text-field {:class "form-control"} "event")]
           [:div {:class "form-group"}
            (hf/label {:class "control-label"} "location" "Location")
            (hf/text-field {:class "form-control"} "location")]
           [:div {:class "form-group"}
            (hf/label {:class "control-label"} "date" "Date")
            (hf/text-field {:class "form-control"} "date")]
           [:div {:class "form-group"}
            (hf/label {:class "control-label"} "format" "Format")
            (hf/text-field {:class "form-control"} "format")]
           [:div {:class "form-group"}
            (hf/label {:class "control-label"} "place" "Finish")
            (hf/text-field {:class "form-control"} "place")]
           [:br]
           [:div {:class "btn-group" :role "group"}
            (hf/submit-button {:class "btn btn-default"} "Submit")
            (hf/reset-button {:class "btn btn-default"} "Clear")]])]])

(def upload-html
  [:div {:class "content"}
   [:h1 "Upload Data"]
   [:p "Upload a CSV file of placings to add to the database. It must have tab-delimited
        fields in the following order:"]
   [:p "Player_name, deck_name, date, location, place, deck_id, event, format"]
   [:div {:class "well"}
    (into (hf/form-to {:class "form" :role "form" :enctype "multipart/form-data"} [:put "/placings"])
          [[:div {:class "form-group"}
            (hf/label {:class "control-label"} "file" "Upload file")
            (hf/file-upload {:class "form-control"} "file")]
           [:br]
           [:div {:class "btn-group" :role "group"}
            (hf/submit-button {:class "btn btn-default"} "Submit")]])]])