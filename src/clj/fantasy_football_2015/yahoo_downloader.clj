(ns fantasy-football-2015.yahoo-downloader
  (:require [net.cgrand.enlive-html :as html]
            [clojure.java.io :as io]))

(def url-template "https://football.fantasysports.yahoo.com/f1/draftanalysis?tab=AD&pos=ALL&sort=DA_AP&count=")

(defn parse-table-row [row]
  (let [[name-team-position-cell _ avg-cost-cell _] (html/select row [:td])
        name (html/text (first (html/select name-team-position-cell [:.name])))
        team-position (first (html/select name-team-position-cell [:.Fz-xxs]))
        [_ team position] (re-matches #"^([^ ]{2,3}) - ([^ ]+)$" (html/text team-position))
        value (read-string (clojure.string/replace (html/text avg-cost-cell) "$" ""))]
    {:name name
     :position position
     :team team
     :value value}))

(defn -main []
  (let [url (str url-template "0") ;; TODO fetch additional pages
        resource (html/html-resource (java.net.URL. url-template))
        table (html/select resource [:table#draftanalysistable])
        table-rows (html/select table [:tbody :tr])]
    (prn (html/select  (first table-rows) [:td]))
    (prn (map parse-table-row table-rows))))
