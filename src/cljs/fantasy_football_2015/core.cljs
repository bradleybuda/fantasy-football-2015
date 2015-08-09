(ns fantasy-football-2015.core
    (:require [reagent.core :as reagent]))

;; TODO should I be division-aware? Might be useful to penalize my
;; division opponents more


(def roster-composition
  [:qb :rb :rb :wr :wr :te :flex :d/st :k
   :bench :bench :bench :bench :bench :bench :bench])

;; TODO update with proper order
(def members-in-draft-order
  [:larry-whalen :david-baum :blake-wilson :michael-aslanides
   :kevin-worley :will-morton :bradley-buda :sam-strasfeld
   :jeff-mazer :eric-eleton :nick-horton :josh-harris])

(def all-players
  ["Alice" "Bob" "Charlie"])

(defonce app-state
  (reagent/atom
   {:available-players all-players
    :picked-players []}))

(def me :bradley-buda)

(defn members-table []
  [:table
   [:th "Draft Selections"]
   (for [[member-idx member] (map-indexed (fn [member-idx member] [member-idx (str member)]) members-in-draft-order)]
     ^{:key member-idx}
     [:tr
      [:th member]
      (for [[idx roster-position] (map-indexed (fn [idx item] [idx (str item)]) roster-composition)]
        ^{:key idx} [:td roster-position])])])

(defn players-table []
  [:table
   [:th "Available Players"]
   (for [player all-players]
     [:tr [:td player]])])

(defn page []
  [:div
   [members-table]
   [players-table]]
  )

(defn ^:export main []
  (reagent/render [page]
                  (.getElementById js/document "app")))
