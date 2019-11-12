(defproject task-cabinet-server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 ;; for handler
                 [ring/ring-jetty-adapter "1.7.1"]
                 [metosin/reitit "0.3.10"]
                 [ring-cors "0.1.13"]
                 [ring-logger "1.0.1"]
                 [com.fasterxml.jackson.core/jackson-core "2.10.0"] ;; required!!!
                 ;; for security
                 [buddy/buddy-hashers "1.4.0"]
                 ;; for json
                 [clj-time "0.15.2"]
                 [cheshire "5.9.0"]
                 ;; to deal with  environment variables
                 [environ "1.1.0"]
                 ;; for integrant-repl
                 [integrant "0.7.0"]
                 [integrant/repl "0.3.1"]
                 ;; for logging
                 [com.taoensso/timbre "4.10.0"]
                 [com.fzakaria/slf4j-timbre "0.3.14"]
                 ;; ;; for database
                 ;; [honeysql "0.9.8"]
                 ;; [seancorfield/next.jdbc "1.0.9"]
                 ;; [hikari-cp "2.9.0"]
                 ;; [org.postgresql/postgresql "42.2.5"]
                 ;; ;; for migration
                 ;; [ragtime "0.8.0"]

                 ;; for others
                 [camel-snake-kebab "0.4.0"]]
  :main ^:skip-aot task-cabinet-server.core
  :target-path "target/%s"
  :plugins [[lein-environ "1.1.0"]
            [cider/cider-nrepl "0.23.0-SNAPSHOT"]]
  :profiles {:uberjar {:aot :all}
             :dev [:project/dev :profiles/dev]
             :test [:project/dev :profiles/test]
             :profiles/dev {}
             :profiles/test {}
             }
  :repl-options
  {:host "0.0.0.0"
   :port 39998})
