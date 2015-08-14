(ns fantasy-football-2015.toy)

(def roster-composition
  [:qb :rb :rb :wr :te :flex :k :bench :bench])

;; TODO update with proper order
(def members-in-draft-order
  [:larry-whalen :david-baum :bradley-buda :blake-wilson])

;; TODO
(def all-players
  (repeatedly 100 #(rand-int 100000)))
