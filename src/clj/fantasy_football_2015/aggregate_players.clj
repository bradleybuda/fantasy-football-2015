(ns fantasy-football-2015.aggregate-players
  (:require [fantasy-football-2015.generated.cbs]
            [fantasy-football-2015.generated.espn]
            [fantasy-football-2015.generated.yahoo]))

;; TODO more clojure-y way to do this?

(def player-lists
  {:cbs fantasy-football-2015.generated.cbs/all-players
   :espn fantasy-football-2015.generated.espn/all-players
   :yahoo fantasy-football-2015.generated.yahoo/all-players})

(defn all-player-names []
  (set (map :name (flatten (vals player-lists)))))

;; Can return nil
;; TODO fuzzy match
(defn find-player-by-name [name player-list]
  (first (filter #(= name (:name %1)) player-list)))

(defn map-hash-map [f hash]
  (reduce (fn [updated-hash [k v]]
            (assoc updated-hash k (f k v))) {} hash))

(defn find-matching-players [player-name]
  (map-hash-map
   (fn [_ player-list]
     (find-player-by-name player-name player-list))
   player-lists))

(defn extract-single-value [player-name matching-players key]
  (let [matching-player-vals (vals matching-players)
        values (map key matching-player-vals)
        uniq-values (set (remove nil? values))]
    (if (> (count uniq-values) 1)
      (println (str "found multiple " key " for " player-name)))
    (first uniq-values)))

(defn build-player-by-name [player-name]
  (let [matching-players (find-matching-players player-name)]
    {:name player-name
     :position (extract-single-value player-name matching-players :position)
     :team (extract-single-value player-name matching-players :team)
     :values (map :value (vals matching-players))}))

(defn normalize-player-values [max-values player]
  (update-in player [:values]
             (fn [values]
               (map (fn [[value max-value]]
                      (if (nil? value)
                        0
                        (int (* 100 (/ value max-value)))))
                    (map vector values max-values)))))

(defn build-player-list []
  (let [player-list (map build-player-by-name (all-player-names))
        all-values (map :values player-list)
        columnar-values (apply map vector all-values)
        max-values (map #(apply max (remove nil? %1)) columnar-values)]
    (map (partial normalize-player-values max-values) player-list)))

(defn -main []
  (prn (build-player-list)))
