(defproject mtg-info "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.2"]
                 [liberator "0.12.2"]
                 [cheshire "5.3.1"]
                 [compojure "1.2.1"]
                 [clj-time "0.8.0"]
                 [compojure "1.2.1"]
                 [prismatic/plumbing "0.3.5"]]
  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler mtg-info.core.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}}
)
