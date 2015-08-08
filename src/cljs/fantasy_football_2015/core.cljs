(ns fantasy-football-2015.core
    (:require [reagent.core :as reagent]))

(defonce app-state (reagent/atom {:text "Hello, what is your name? "}))

(def roster-composition
  [:qb :rb :rb :wr :wr :te :flex :d/st :k
   :bench :bench :bench :bench :bench :bench :bench])

(defn page []
  [:table
   [:tr
    (for [[idx roster-position] (map-indexed (fn [idx item] [idx (str item)]) roster-composition)]
      ^{:key idx} [:td roster-position])
    ]])

(defn ^:export main []
  (reagent/render [page]
                  (.getElementById js/document "app")))
