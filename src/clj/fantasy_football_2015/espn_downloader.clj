(ns fantasy-football-2015.espn-downloader
  (:require [net.cgrand.enlive-html :as html]
            [clojure.java.io :as io]))

(def page-to-scrape "http://espn.go.com/fantasy/football/story/_/id/12866396/top-300-rankings-2015")
(def output-file "src/cljs/fantasy_football_2015/generated/espn.cljs")

(defn parse-table-row [row]
  (let [cells (html/select row [:td])
        [rank-name-position team bye posrank value] (map html/text cells)
        [_ rank name position] (re-matches #"^(\d+)\. ([^,]+), (.*)$" rank-name-position)]
    {:name name
     :position position
     :team team
     :rank (read-string rank)
     :posrank (read-string (second (re-find #"(\d+)" posrank)))
     :value (if (= "--" value) 0 (read-string (clojure.string/replace value "$" "")))}))

(defn -main []
  (let [resource (html/html-resource (java.net.URL. page-to-scrape))
        table (second (html/select resource [:table.inline-table]))
        table-rows (html/select table [:tbody :tr])
        players (vec (map parse-table-row table-rows))]

    (with-open [wrtr (io/writer output-file)]
      (.write wrtr (prn-str '(ns fantasy-football-2015.generated.espn)))
      (.write wrtr (prn-str `(def ~'all-players ~players))))))
