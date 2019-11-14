(ns task-cabinet-server.Boundary.utils.migrate
  (:require [ragtime.jdbc :as jdbc]
            [clojure.java.io :as io]
            [clj-time.core :as time]
            [clj-time.coerce :as tc]
            [clojure.string :as string]
            [integrant.core :as ig]
            [taoensso.timbre :as timbre]
            [ragtime.repl :as rrepl]))


(def default-migration-folder "migrations")

(def default-migration-log
  {:dev "dev-migration.log"
   :test "test-migration.log"
   :prod "prod-migration.log"})

(def default-migration-option
  {:encoding "UTF-8"
   :append true})


(defn init-file! [fname]
  (let [{:keys [encoding append]} default-migration-option]
    (when-not (.exists (io/as-file fname))
      (timbre/info "first logging ... ")
      (spit fname "first-log\n" :encoding encoding :append append))))

(defn migrate! [command database-url migration-log]
  (let [{:keys [encoding append]}
        default-migration-option
        config
        {:datastore (jdbc/sql-database {:connection-uri database-url})
         :migrations (jdbc/load-resources default-migration-folder)}]
    (condp = command
      :up (do
            (timbre/info "migration start!")
            (rrepl/migrate config)
            (spit migration-log (str "migrated! at " (tc/to-string (time/now)) "\n")
                  :encoding encoding :append append))
      ;;
      ;; if you want to rollback when halt your system, please enable it.
      ;; :down (do
      ;;         (timbre/info "rollback migration!")
      ;;         (rrepl/rollback config (count (:migrations config)))
      ;;         (spit migration-log (str "rollback! at " (tc/to-string (time/now)) "\n")
      ;;               :encoding encoding :append append))
      nil
      )))

(defmethod ig/init-key ::migrate
  [_ {:keys [env] :as options}]
  (println env)
  (let [{:keys [running database-url]} env
        migration-log (get default-migration-log (keyword running))
        log (do
              (init-file! migration-log)
              (string/split-lines (slurp migration-log)))
        migrated? false ;;(string/starts-with? (last log) "migrated! at ")
        ]
    (timbre/info "load migration file: " migration-log)
    (timbre/info "check migrated?" migrated?)
    (if-not migrated?
      (migrate! :up database-url migration-log)
      (timbre/info "keep database as is"))
    options))

(defmethod ig/halt-key! ::migrate
  [_ {:keys [env] :as options}]
  (let [{:keys [running database-url]} env
        migration-log (get default-migration-log (keyword running))
        log (do
              (init-file! migration-log)
              (string/split-lines (slurp migration-log)))
        migrated? (string/starts-with? (last log) "migrated! at ")]
    (when migrated?
      (migrate! :down database-url migration-log))))

;; for debug
(defn reset-migrate []
  (let [database-url (environ.core/env :database-url)
        config  {:datastore (jdbc/sql-database {:connection-uri database-url})
                 :migrations (jdbc/load-resources default-migration-folder)}]
    (rrepl/rollback config (count (:migrations config)))
    (rrepl/migrate config)))

;; (reset-migrate)


