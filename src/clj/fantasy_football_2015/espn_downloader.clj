(ns fantasy-football-2015.espn-downloader
  (:require [net.cgrand.enlive-html :as html]))

(def page-to-scrape "http://espn.go.com/fantasy/football/story/_/id/12866396/top-300-rankings-2015")

(defn parse-table-row [row]
  (let [cells (html/select row [:td])
        [rank-player-position team bye posrank value] (map html/text cells)
        [_ rank player position] (re-matches #"^(\d+)\. ([^,]+), (.*)$" rank-player-position)]
    {:rank (read-string rank)
     :player player
     :position position
     :team team
     :value (if (= "--" value) 0 (read-string (clojure.string/replace value "$" "")))}))

(defn -main []
  (let [resource (html/html-resource (java.net.URL. page-to-scrape))
        table (second (html/select resource [:table.inline-table]))
        table-rows (html/select table [:tbody :tr])]
    (pr (map parse-table-row table-rows))))
