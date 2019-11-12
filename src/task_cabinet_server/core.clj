(ns task-cabinet-server.core
  (:gen-class)
  (:require [environ.core :refer [env]]
            [taoensso.timbre :as timbre]
            [clojure.java.io :as io]
            [integrant.core :as ig]
            [integrant.repl :as igr]))


(timbre/set-level! :info)

(def config-file
  "config.edn")

(defn load-config [config]
  (-> config
      io/resource
      slurp
      ig/read-string
      (doto
          ig/load-namespaces)))

;; (load-config config-file)

(defn start []
  (igr/set-prep! (constantly (load-config config-file)))
  (igr/prep)
  (igr/init))

(defn stop []
  (igr/halt))

(defn restart []
  (igr/reset))

(defn restart-all []
  (igr/reset-all))

(defn -main
  [& args]
  (timbre/set-level! :info)
  (-> config-file
      load-config
      ig/init))
