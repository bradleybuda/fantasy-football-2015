(ns fantasy-football-2015.core
  (:require [reagent.core :as reagent]
            [
             ;;fantasy-football-2015.bastards
             fantasy-football-2015.toy
             :refer [roster-composition members-in-draft-order all-players]]))

;; TODO should I be division-aware? Might be useful to penalize my
;; division opponents more

;; TODO actually reference this in views
(defonce app-state
  (reagent/atom
   {:available-players all-players
    :roster-composition roster-composition
    :members-in-draft-order members-in-draft-order
    :picked-players []}))

(def me :bradley-buda)

;; zero-based
(defn next-pick-index []
  (count (:picked-players @app-state)))

(defn snake-pick-sequence []
  (let [draft-order (:members-in-draft-order @app-state)]
    (cycle (concat draft-order (reverse draft-order)))))

(defn next-pick []
  (nth (snake-pick-sequence) (next-pick-index)))

(defn members-table []
  [:table.members-table
   [:th "Draft Selections"]
   (for [[member-idx member] (map-indexed (fn [member-idx member] [member-idx (str member)]) members-in-draft-order)]
     ^{:key member-idx}
     [:tr
      {:class
       (str
        (if (= member (str me)) "me" "opponent")
        (if (= member (str (next-pick))) " next-pick"))}
      [:th member]
      (for [[idx roster-position] (map-indexed (fn [idx item] [idx (str item)]) roster-composition)]
        ^{:key idx} [:td roster-position])])])

(defn players-table []
  [:table
   [:th "Available Players"]
   (for [player all-players]
     [:tr [:td player]])])

(defn page []
  [:div.page
   [:div.members
    [members-table]]
   [:div.players
    [players-table]]])

(defn ^:export main []
  (reagent/render [page]
                  (.getElementById js/document "app")))
