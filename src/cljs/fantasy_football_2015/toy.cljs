(ns fantasy-football-2015.toy)

(def roster-composition
  [:qb :rb :rb :wr :te :flex :k :bench :bench])

;; TODO update with proper order
(def members-in-draft-order
  ["Larry Whalen" "David Baum" "Bradley Buda" "Blake Wilson"])

;; TODO
(def all-players
  (repeatedly 80 #(rand-int 100000)))
