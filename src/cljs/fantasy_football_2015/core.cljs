(ns fantasy-football-2015.core
  (:require [reagent.core :as reagent]
            [clojure.set :refer [difference]]
            [fantasy-football-2015.generated.espn :refer [all-players]]
            [
             ;;fantasy-football-2015.bastards
             fantasy-football-2015.toy
             :refer [roster-slots members-in-draft-order]
             ]))

(enable-console-print!)

;; TODO should I be division-aware? Might be useful to penalize my
;; division opponents more

(def me "Bradley Buda")

;; zero-based
(defn next-pick-index [app-state]
  (count (:picked-players app-state)))

(defn snake-pick-sequence [app-state]
  (let [draft-order (:members-in-draft-order app-state)]
    (cycle (concat draft-order (reverse draft-order)))))

(defn next-member-to-pick [app-state]
  (nth (snake-pick-sequence app-state) (next-pick-index app-state)))

(defn picked-players-for-member [member app-state]
  (let [picked-players (:picked-players app-state)
        picked-players-with-members (map vector picked-players (snake-pick-sequence app-state))]
    (map first
         (filter (fn [[_ picking-member] _]
                   (= member picking-member))
                 picked-players-with-members))))

(defn unpicked-players [app-state]
  (difference (set all-players) (set (:picked-players app-state))))

(defn player-can-fill-roster-slot? [slot player]
  (condp = slot
    "Bench" true
    "Flex" (contains? #{"RB" "WR" "TE"} (:position player))
    (= slot (:position player))))

(defn best-player-for-slot [slot players]
  (let [eligible-players (filter (partial player-can-fill-roster-slot? slot) players)]
    (apply max-key :value eligible-players)))

(defn- roster-from-players-recursive [{:keys [roster open-slots unassigned-players]}]
  (if (empty? open-slots)
    roster
    (let [[slot & remaining-slots] open-slots
          player-for-slot (best-player-for-slot slot unassigned-players)]
      (roster-from-players-recursive
       {:roster (conj roster player-for-slot)
        :open-slots remaining-slots
        :unassigned-players (disj unassigned-players player-for-slot)}))))

(defn roster-from-players [players]
  (roster-from-players-recursive
   {:roster []
    :open-slots roster-slots
    :unassigned-players (set players)}))

(defonce app-state
  (reagent/atom
   {:available-players all-players
    :members-in-draft-order members-in-draft-order
    :picked-players []}))

;; Actions

(defn pick-player [player]
  (swap! app-state
         (fn [state]
           (update-in state [:picked-players] #(conj %1 player)))))


;; Views

(defn members-table []
  (let [state @app-state
        next-member-to-pick (next-member-to-pick state)]
    [:table.members-table
     [:thead
      [:tr
       [:th "Member"]
       (for [[slot-index slot] (map-indexed vector roster-slots)]
         ^{:key slot-index}
         [:th slot])]]
     [:tbody
      (for [member members-in-draft-order]
        ^{:key member}
        [:tr
         {:class
          (str
           (if (= member me) "me" "opponent")
           (if (= member next-member-to-pick) " next-pick"))}
         [:th member]
         (for [[roster-index player] (map-indexed vector (roster-from-players (picked-players-for-member member state)))]
           ^{:key roster-index}
           [:td (or (:name player) [:i "empty"])])])]]))

(defn players-table []
  (let [state @app-state]
    [:table
     [:tbody
      [:tr
       [:th "Player"]
       [:th "Position"]
       [:th "Rank"]
       [:th "Value"]
       [:td]]
      (for [player (reverse (sort-by :value (unpicked-players state)))]
        ^{:key (:name player)}
        [:tr
         [:td (:name player)]
         [:td (:position player)]
         [:td (:rank player)]
         [:td (:value player)]
         [:td [:button {:on-click (partial pick-player player)} "Draft"]]])]]))

(defn page []
  [:div.page
;;   [:pre.debug (pr-str @app-state)]
   [:div.members
    [members-table]]
   [:div.players
    [players-table]]])

;; main

(defn ^:export main []
  (reagent/render [page]
                  (.getElementById js/document "app")))
