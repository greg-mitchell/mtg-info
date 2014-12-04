(ns mtg-info.resources.util
  (:require [clojure.java.io :as io]
            [hiccup.core :as h]))

(defn body-as-string [ctx]
  (if-let [body (get-in ctx [:request :body])]
    (condp instance? body
      String body
      (slurp (io/reader body)))))

(defn parse-body [context key parse-fn]
  (when (#{:put :post} (get-in context [:request :request-method]))
    (try
      (if-let [body (body-as-string context)]
        (let [data (parse-fn body)]
          [false {key data}])
        {:message "No body"})
      (catch Exception e
        (.printStackTrace e)
        {:message (format "IOException: %s" (.getMessage e))}))))

(defn check-content-type [ctx content-types]
  (if (#{:put :post} (get-in ctx [:request :request-method]))
    (or
      (some #{(get-in ctx [:request :headers "content-type"])}
            content-types)
      [false {:message "Unsupported Content-Type"}])
    true))

(defn map-tag
  ([tag xs]
   (map (fn [x] [tag x]) xs))
  ([tag attr xs]
   (map (fn [x] [tag attr x]) xs)))

