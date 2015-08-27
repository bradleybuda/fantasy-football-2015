(ns fantasy-football-2015.cbs-downloader
  (:require [net.cgrand.enlive-html :as html]
            [clojure.java.io :as io]))

(def page-to-scrape "http://fantasynews.cbssports.com/fantasyfootball/rankings/yearly/ppr")
(def output-file "src/cljs/fantasy_football_2015/generated/cbs.cljs")

(def translate-position
  {"Quarterbacks" "QB"
   "Running Backs" "RB"
   "Wide Receivers" "WR"
   "Tight Ends" "TE"
   "Kickers" "K"
   "Defensive Special Teams" "DST"})

(defn- parse-player-row [row position-s]
  (let [position (translate-position position-s)
        [rank-cell name-cell] (html/select row [:td])
        posrank (html/text rank-cell)
        name (clojure.string/trim (html/text (first (html/select name-cell [:a]))))
        [_ value] (re-find #" \$(\d+) " (html/text name-cell))
        [_ team] (re-find #"\W([A-Z]{2,3})\W" (html/text name-cell))]
    {:name (if (= "DST" position) team name)
     :position position
     :team (if (= team "III") "WAS" team) ;; RG3 hack
     :posrank (read-string posrank)
     :value (if value (read-string value) 0)}))

(defn- parse-table-rows
  ([table-rows] (parse-table-rows table-rows nil []))
  ([table-rows current-position players]
   (let [[row & remaining-rows] table-rows]
     (if (nil? row)
       players
       (if (= "title" (:class (:attrs row)))
         (recur remaining-rows (html/text row) players)
         (recur remaining-rows current-position
                (conj players (parse-player-row row current-position))))))))

(defn -main []
  (let [resource (html/html-resource (java.net.URL. page-to-scrape))
        table (first (html/select resource [:table.data]))
        table-rows (html/select table [:tr])
        players (parse-table-rows table-rows)]

    (with-open [wrtr (io/writer output-file)]
      (.write wrtr (prn-str '(ns fantasy-football-2015.generated.cbs)))
      (.write wrtr (prn-str `(def ~'all-players ~players))))))
