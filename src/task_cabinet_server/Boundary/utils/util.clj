(ns task-cabinet-server.Boundary.utils.util
  (:require
   [next.jdbc :as jdbc]
   [clj-time.core :as t]
   [clj-time.coerce :as tc]
   [clojure.string :as string]
   [next.jdbc.sql :as njs]
   [buddy.hashers :as hashers]
   [camel-snake-kebab.core :refer [->kebab-case ->snake_case]]
   [next.jdbc.result-set :as rs]
   [camel-snake-kebab.extras :refer [transform-keys]]))

(defn match-regex [regex s]
  (re-matches regex s))

(defn check-whitespace [s]
  (= (count s) (count (string/trim s))))

(defn hash-password [password]
  (hashers/derive password))

 (defn check-identity [password pass-list]
  (filter #(hashers/check password (:users/password %)) pass-list))

(defn long-to-sql [long-time]
  (-> long-time
      tc/from-long
      tc/to-sql-time))

(defn sql-to-long [sql-time]
  (-> sql-time
      tc/from-sql-time
      tc/to-long))

(defn sql-now []
  (tc/to-sql-time (t/now)))

(defn transform-keys-to-snake [m]
  (transform-keys #(->snake_case % :separator \-) m))

(defn transform-keys-to-kebab [m]
  (transform-keys #(->kebab-case % :separator \_) m))

(defn run-sql [spec sql-command-list one?]
  (with-open [conn (jdbc/get-connection (:datasource spec))]
    (if one?
      (jdbc/execute-one! conn sql-command-list)
      (jdbc/execute! conn sql-command-list))))

(defn insert! [spec table-key m]
  (with-open [conn (jdbc/get-connection (:datasource spec))]
    (println "m" m)
    (njs/insert! conn table-key m {:return-keys true :builder-fn rs/as-unqualified-lower-maps} )))

(defn update! [spec table-key m idm]
  (with-open [conn (jdbc/get-connection (:datasource spec))]
    (:next.jdbc/update-count (njs/update! conn table-key (assoc m :updated_at (sql-now))  idm))))

(defn delete! [spec table-key idm]
  (with-open [conn (jdbc/get-connection (:datasource spec))]
    (:next.jdbc/update-count (njs/delete! conn table-key idm))))

(defn find-by-m [spec table-key m]
  (with-open [conn (jdbc/get-connection (:datasource spec))]
    (njs/find-by-keys conn table-key m {:return-keys true :builder-fn rs/as-unqualified-lower-maps} )))

(defn get-by-id [spec table-key k v]
  (println "get! "k v)
  (with-open [conn (jdbc/get-connection (:datasource spec))]
    (njs/get-by-id conn table-key v k {:return-keys true :builder-fn rs/as-unqualified-lower-maps} )))




(defonce connecter (jdbc/get-datasource {:jdbcUrl  "jdbc:postgresql://dev_db:5432/tcs_db?user=meguru&password=emacs"}))

;; (jdbc/execute-one! connecter ["SELECT * FROM users where id = 1"])
 (njs/update! connecter :users {:email "meguru.mokke@gmail.com" :is_deleted false :name "MokkeMeguru" } {:id 1}) 
 (jdbc/execute! connecter ["SELECT * FROM user_token"])


;; (jdbc/execute! connecter ["SELECT * FROM user_device"])
 ;; (-> (sql-now) sql-to-long println )
(jdbc/execute! connecter ["SELECT * FROM task where id = 7"])
(njs/find-by-keys connecter :task {:id 2, :user_id 1})

