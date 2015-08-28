(ns fantasy-football-2015.yahoo-downloader
  (:require [net.cgrand.enlive-html :as html]
            [clojure.java.io :as io]))

(def url-template "https://football.fantasysports.yahoo.com/f1/draftanalysis?tab=AD&pos=ALL&sort=DA_AP&count=")
(def output-file "src/clj/fantasy_football_2015/generated/yahoo.clj")

(defn parse-table-row [row]
  (let [[name-team-position-cell _ avg-cost-cell _] (html/select row [:td])
        name (html/text (first (html/select name-team-position-cell [:.name])))
        team-position (first (html/select name-team-position-cell [:.Fz-xxs]))
        [_ team-s position-s] (re-matches #"^([^ ]{2,3}) - ([^ ]+)$" (html/text team-position))
        team (if (= team-s "Jax") "JAC" (clojure.string/upper-case team-s))
        position (if (= position-s "DEF") "DST" position-s)
        value-str (clojure.string/replace (html/text avg-cost-cell) "$" "")
        value (if (= "-" value-str) 0 (/ (int (* 10 (read-string value-str))) 10))]

    {:name (if (= "DST" position) team name)
     :position position
     :team team
     :value value}))

(defn -main []
  (let [players (vec
                 (mapcat
                  (fn [offset]
                    (let [url (str url-template offset)
                          resource (html/html-resource (java.net.URL. url))
                          table (html/select resource [:table#draftanalysistable])
                          table-rows (html/select table [:tbody :tr])]
                      (map parse-table-row table-rows)))
                  (range 0 300 50)))]

    (with-open [wrtr (io/writer output-file)]
      (.write wrtr (prn-str '(ns fantasy-football-2015.generated.yahoo)))
      (.write wrtr (prn-str `(def ~'all-players ~players))))))
