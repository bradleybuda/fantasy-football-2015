(ns fantasy-football-2015.core
  (:require [reagent.core :as reagent]
            [
             ;;fantasy-football-2015.bastards
             fantasy-football-2015.toy
             :refer [roster-composition members-in-draft-order all-players]]))

(enable-console-print!)

;; TODO should I be division-aware? Might be useful to penalize my
;; division opponents more


;; TODO more consitent state handling - don't access it as a global

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

(defn next-member-to-pick []
  (nth (snake-pick-sequence) (next-pick-index)))

(defn picked-players-for-member [member picked-players]
  (let [picked-players-with-members (map vector picked-players (snake-pick-sequence))]
    (map first
         (filter (fn [[_ picking-member] _]
                   (= member picking-member))
                 picked-players-with-members))))

;; (defn roster-for-member [member picked-players]
;;   (let [players-for-member (picked-players-for-member member pick-players)]

;;     )
;;   )

;; (defn remaining-roster-for-member
;;   [member remaining-players remaining-roster-composition])

(defn members-table []
  (let [next-member-to-pick (next-member-to-pick)
        picked-players (:picked-players @app-state)]
    [:table.members-table
     [:th "Draft Selections"]
     (for [member members-in-draft-order]
       ^{:key member}
       [:tr
        {:class
         (str
          (if (= member me) "me" "opponent")
          (if (= member next-member-to-pick) " next-pick"))}
        [:th member]
        (for [picked-player (picked-players-for-member member picked-players)]
          [:span (:name picked-player)])])]))

(defn pick-player [player]
  (swap! app-state
         (fn [state]
           (update-in state [:picked-players] #(conj %1 player)))))

(defn players-table []
  [:table
   [:tr
    [:th "Player"]
    [:th "Position"]
    [:td]]
   (for [[player-idx player] (map-indexed vector all-players)]
     ^{:key player-idx}
     [:tr
      [:td (:name player)]
      [:td (:position player)]
      [:td [:button {:on-click (partial pick-player player)} "Draft"]]])])

(defn page []
  [:div.page
;;   [:pre.debug (pr-str @app-state)]
   [:div.members
    [members-table]]
   [:div.players
    [players-table]]])

(defn ^:export main []
  (reagent/render [page]
                  (.getElementById js/document "app")))
