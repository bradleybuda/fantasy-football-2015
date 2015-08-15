(ns fantasy-football-2015.toy
  (:require [clojure.set :refer [difference]]))

(def roster-composition
  ["QB" "RB" "RB" "WR" "TE" "Flex" "K" "Bench" "Bench"])

(def positions
  (difference (set roster-composition) #{"Flex" "Bench"}))

;; TODO update with proper order
(def members-in-draft-order
  ["Larry Whalen" "David Baum" "Bradley Buda" "Blake Wilson"])

;; TODO
(def all-players
  (repeatedly 80
              (fn []
                {:name (rand-int 1000000)
                 :position (rand-nth (seq positions))})))
