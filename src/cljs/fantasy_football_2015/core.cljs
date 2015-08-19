(ns fantasy-football-2015.core
  (:require [reagent.core :as reagent]
            [clojure.set :refer [difference]]
            [fantasy-football-2015.generated.cbs]
            [fantasy-football-2015.generated.espn]
            [
             ;;fantasy-football-2015.bastards
             fantasy-football-2015.toy
             :refer [roster-slots members-in-draft-order]
             ]))

(enable-console-print!)

;; Wishlist

;; Show multiple ratings per player
;; Normalize ratings
;; Aggregate ratings
;; Show team rating so far
;; Show best possible team rating
;; Show delta team ratings against others
;; Weight delta ratings by actual matchups (want to win against people I'm playing)
;; Weight earlier matchups heavier
;; Navigate away warning if any picks made
;; Undo pick

(def all-players
  (loop [cbs-players (set fantasy-football-2015.generated.cbs/all-players)
         espn-players (set fantasy-football-2015.generated.espn/all-players)
         merged-players []]
    (let [[cbs-player & remaining-cbs-players] (seq cbs-players)]
      (if (nil? cbs-player)
        ;; TODO warn about remaining espn players
        merged-players

        (let [espn-player (first (filter #(= (:name cbs-player) (:name %1)) espn-players))]

          (if (nil? espn-player)
            (println (str "no matching espn player for " (pr-str cbs-player)))
            (println (pr-str espn-player)))
          (recur remaining-cbs-players (disj espn-players espn-player) (conj merged-players (assoc cbs-player :espn (or espn-player {})))))))))


(def me "Bradley Buda")

;; zero-based
(defn next-pick-index [{:keys [picked-players]}]
  (count picked-players))

(defn snake-pick-sequence [{:keys [members-in-draft-order]}]
  (cycle (concat members-in-draft-order (reverse members-in-draft-order))))

(defn next-member-to-pick [app-state]
  (nth (snake-pick-sequence app-state) (next-pick-index app-state)))

(defn picked-players-for-member [member app-state]
  (let [picked-players (:picked-players app-state)
        picked-players-with-members (map vector picked-players (snake-pick-sequence app-state))]
    (map first
         (filter (fn [[_ picking-member] _]
                   (= member picking-member))
                 picked-players-with-members))))

(defn unpicked-players [{:keys [picked-players]}]
  (difference (set all-players) (set picked-players)))

(defn player-can-fill-roster-slot? [slot player]
  (condp = slot
    "Bench" true
    "Flex" (contains? #{"RB" "WR" "TE"} (:position player))
    (= slot (:position player))))

(defn best-player-for-slot [slot players]
  (let [eligible-players (filter (partial player-can-fill-roster-slot? slot) players)]
    (apply max-key :value eligible-players)))

(defn roster-from-players
  ([players] (roster-from-players (set players) [] roster-slots))
  ([unassigned-players roster-so-far remaining-slots]
   (if (empty? remaining-slots)
     roster-so-far
     (let [[slot & remaining-slots] remaining-slots
           player-for-slot (best-player-for-slot slot unassigned-players)]
       (recur
        (disj unassigned-players player-for-slot)
        (conj roster-so-far player-for-slot)
        remaining-slots)))))

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
       [:th "Team"]
       [:th "Position"]
       [:th "CBS Value"]
       [:th "ESPN Value"]
       [:td]]
      (for [player (reverse (sort-by :value (unpicked-players state)))]
        ^{:key (:name player)}
        [:tr
         [:td (:name player)]
         [:td (:team player)]
         [:td (:position player)]
         [:td (:value player)]
         [:td (:value (:espn player))]
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
