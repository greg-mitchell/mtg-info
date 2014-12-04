(ns mtg-info.core.data
  (:require [clojure.string :as s]))

(defrecord Placing [name deck date event location place id])

(defn parse-line [line]
  (->> (s/split line #"\t")
       ;; TODO: parse date
       (apply ->Placing)))

(defonce data (atom nil))

(defn load-data [^String body]
  (->> (s/split-lines body)
       (map parse-line)
       (doall)
       (reset! data)))
