(ns mtg-info.layout.html
  (:require [hiccup.page :as hp]
            [hiccup.element :as he]
            [hiccup.form :as hf]))

;; TODO: fix active
(defn navbar []
  [:nav {:class "navbar navbar-default navbar-fixed-top" :role "navigation"}
   [:div {:class "container"}
    [:div {:class "navbar-header"}
     [:button {:type "button" :class "navbar-toggle collapsed" :data-toggle "collapse"
               :data-target "#navbar"}
      [:span {:class "sr-only"} "Toggle navigation"]
      [:span {:class "icon-bar"}]]
     [:a {:class "navbar-brand" :href "/"} "MTG Infobank"]]
    [:div {:id "navbar" :class "navbar-collapse collapse"}
     [:ul {:class "nav navbar-nav"}
      [:li {:class "active"} (he/link-to "/" "Search")]
      [:li (he/link-to "/submit" "Submit Data")]]]]])

(defn application
  [title & content]
  (hp/html5 {:ng-app "myApp" :lang "en"}
            [:head
             [:title title]
             (hp/include-css "//code.jquery.com/ui/1.11.2/themes/smoothness/jquery-ui.css"
                             "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap.min.css"
                             "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap-theme.min.css"
                             "css/style.css")
             (hp/include-js "https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js")
             (hp/include-js "//code.jquery.com/ui/1.11.2/jquery-ui.js")
             (hp/include-js "js/script.js")
             [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]]
            [:body
             (navbar)
             [:div {:class "container-fluid"} content]]))

(def hello-html
  [:div {:class "content"}
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
   [:a {:href "/placings?name=Cedric%20Phillips"} "/placings?name=Cedric%20Phillips"]])

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

(def index-html
  [:div {:class "content"}
   [:h1 "MTG Infobank"]
   [:p "Search for player results from the SCG circuit"]
   [:div {:class "well"}
    (into (hf/form-to {:class "form" :role "form"} [:get "/placings"])
          (placings-form-fields
            [:div {:class "form-group"}
             (hf/label {:class "control-label"} "sortby" "Sort-by")
             (reduce conj [:div {:class "radio"}] (map sortby-radio [["Date" "date" true] ["Place" "place" false] ["Name" "name" false]]))]))]])

(def submit-html
  [:div {:class "content"}
   [:h1 "Submit Data"]
   [:p "Add new player data to the database."]
   [:div {:class "well"}
    (into (hf/form-to {:class "form" :role "form"} [:post "/placings"])
          (placings-form-fields))]])
