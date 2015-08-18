(ns fantasy-football-2015.cbs-downloader
  (:require [net.cgrand.enlive-html :as html]
            [clojure.java.io :as io]))

(def page-to-scrape "http://fantasynews.cbssports.com/fantasyfootball/rankings/yearly/ppr")
(def output-file "src/cljs/fantasy_football_2015/generated/cbs.cljs")

(defn- parse-player-row [row]
  (let [[rank-cell name-cell] (html/select row [:td])
        posrank (html/text rank-cell)
        name (html/text (first (html/select name-cell [:a])))
        [_ value] (re-find #" \$(\d+) " (html/text name-cell))]
    {:name name
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
                (conj players (assoc (parse-player-row row)
                                     :position current-position))))))))

(defn -main []
  (let [resource (html/html-resource (java.net.URL. page-to-scrape))
        table (first (html/select resource [:table.data]))
        table-rows (html/select table [:tr])]
    (prn (parse-table-rows table-rows))))
