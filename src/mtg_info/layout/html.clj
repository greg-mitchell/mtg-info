(ns mtg-info.layout.html
  (:require [hiccup.page :as hp]
            [hiccup.element :as he]
            [hiccup.form :as hf]))

(defn labeled-radio [label checked]
  [:label (hf/radio-button "sortby" checked label)
   (str label "         ")])

(defn application
  [title & content]
  (hp/html5 {:ng-app "myApp" :lang "en"}
            [:head
             [:title title]
             (hp/include-css "//code.jquery.com/ui/1.11.2/themes/smoothness/jquery-ui.css")
             (hp/include-css "//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/css/bootstrap-combined.min.css")
             (hp/include-js "//code.jquery.com/jquery-1.10.2.js")
             (hp/include-js "//code.jquery.com/ui/1.11.2/jquery-ui.js")
             (hp/include-js "js/script.js")]
            [:body
             [:div {:class "container"} content]]))

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
  [:div
   [:h1 {:class "info-worning"} "Page Not Found"]
   [:p "There's no requested page. "]
   (he/link-to {:class "btn btn-primary"} "/" "Take me to Home")])

(def index-html
  [:div {:class "content"}
   [:h1 "MTG Infobank"]
   "Search for player results from the SCG circuit"
   [:br] [:br]
   [:div {:class "well"}
    (hf/form-to [:get "/placings"]
                [:div {:class "form-group"}
                 (hf/label {:class "control-label"} "name" "Player Name")
                 (hf/text-field "name")]
                [:div {:class "form-group"}
                 (hf/label {:class "control-label"} "deck" "Deck Name")
                 (hf/text-field "deck")]
                [:div {:class "form-group"}
                 (hf/label {:class "control-label"} "event" "Event")
                 (hf/text-field "event")]
                [:div {:class "form-group"}
                 (hf/label {:class "control-label"} "location" "Location")
                 (hf/text-field "location")]
                [:div {:class "form-group"}
                 (hf/label {:class "control-label"} "date" "Date")
                 (hf/text-field "date")]
                [:div {:class "form-group"}
                 (hf/label {:class "control-label"} "format" "Format")
                 (hf/text-field "format")]
                [:div {:class "form-group"}
                 (hf/label {:class "control-label"} "place" "Finish")
                 (hf/text-field "place")]
                [:div {:class "form-group"}
                 (hf/label {:class "control-label"} "sortby" "Sort-by")
                 (reduce conj [:div {:class "btn-group"}] (map labeled-radio ["date" "place" "name"] [true false false]))]
                [:br]
                (hf/submit-button "Search")
                (hf/reset-button "Clear"))]])
