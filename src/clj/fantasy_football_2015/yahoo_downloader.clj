(ns fantasy-football-2015.yahoo-downloader
  (:require [net.cgrand.enlive-html :as html]
            [clojure.java.io :as io]))

(def url-template "https://football.fantasysports.yahoo.com/f1/draftanalysis?tab=AD&pos=ALL&sort=DA_AP&count=")

(defn parse-table-row [row]
  {:name
   (html/text (first (html/select row [:div.ysf-player-name :a])))})

(defn -main []
  (let [url (str url-template "0") ;; TODO fetch additional pages
        resource (html/html-resource (java.net.URL. url-template))
        table (html/select resource [:table#draftanalysistable])
        table-rows (html/select table [:tbody :tr])]
    (prn (first table-rows))
    (prn (map parse-table-row table-rows))))
