(defproject fantasy-football-2015 "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-3211"]
                 [reagent "0.5.0"]
                 [enlive "1.1.6"]
                 [net.mikera/core.matrix "0.37.0"]]

  :source-paths ["src/clj"]

  :plugins [[lein-cljsbuild "1.0.6"]
            [lein-figwheel "0.3.7"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {
             :css-dirs ["resources/public/css"]}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"]

                        :figwheel {:on-jsload "fantasy-football-2015.core/main"}

                        :compiler {:main fantasy-football-2015.core
                                   :output-to "resources/public/js/compiled/app.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :asset-path "js/compiled/out"}}

                       {:id "min"
                        :source-paths ["src/cljs"]
                        :compiler {:main fantasy-football-2015.core
                                   :output-to "resources/public/js/compiled/app.js"
                                   :optimizations :advanced
                                   :pretty-print false}}]})
