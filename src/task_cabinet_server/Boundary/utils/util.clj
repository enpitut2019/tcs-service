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
  (with-open [conn (jdbc/get-connection (:datasource spec))]
    (njs/get-by-id conn table-key v k {:return-keys true :builder-fn rs/as-unqualified-lower-maps})))

(defn upsert-builder [table-key m confks update_funcstr]
  (let [ks (map name (keys m))
        confs (map name confks)
        vs (vals m)]
    (concat
     [(clojure.string/join " "
                            ["insert into" (name table-key)
                             "("  (clojure.string/join "," ks) ")"
                             "values (" (clojure.string/join "," (-> vs count (repeat "?"))) ")"
                             "on conflict" "(" (clojure.string/join "," confs) ")"
                             "do" "update set"
                             update_funcstr])]
     vs)))

(defn upsert! [spec table-key m confks update_funcstr]
  (with-open [conn (jdbc/get-connection (:datasource spec))
              upsert-vec (upsert-builder table-key m confks update_funcstr)]
    (jdbc/execute-one! conn upsert-vec)))

;; -------------------------- here is for debug ------------------------------------

;; connection to db
;; (defonce connecter (jdbc/get-datasource {:jdbcUrl  "jdbc:postgresql://dev_db:5432/tcs_db?user=meguru&password=emacs"}))

;; user creation (from web's swagger)
;; check user exististance
;; (jdbc/execute-one! connecter ["SELECT * FROM users where id = 1"])

;; (jdbc/execute-one! connecter
;;                    ;; ["insert into select_alg (user_id, alg, value) VALUES (?,?, 1) on conflict (user_id, alg) do update set value = select_alg.value + 1" (int 1) (int 2)]
;;                    (upsert-builder
;;                     :select_alg
;;                     {:user_id 1 :alg 1 :value 1}
;;                     [:user_id :alg]
;;                     "value = select_alg.value + 1"))

;; (upsert-builder
;;  :select_alg
;;  {:user_id 1 :alg 1 :value 1}
;;  [:user_id :alg]
;;  "value = select_alg.value + 1")

;; (jdbc/execute! connecter ["SELECT * FROM select_alg where user_id = 1"]
;;                {:return-keys true  :builder-fn rs/as-unqualified-lower-maps})

;; (jdbc/execute-one! connecter ["DELETE FROM select_alg"])

;; (let [select-alg-raws (njs/find-by-keys connecter
;;                                         :select_alg
;;                                         {:user_id 1}
;;                                         {:return-keys true :builder-fn rs/as-unqualified-lower-maps})]
;;   select-alg-raws)

;;  (njs/update! connecter :users {:email "meguru.mokke@gmail.com" :is_deleted false :name "MokkeMeguru" } {:id 1}) 
;;  (jdbc/execute! connecter ["SELECT * FROM user_token"])


;; ;; (jdbc/execute! connecter ["SELECT * FROM user_device"])
;;  ;; (-> (sql-now) sql-to-long println )
;; (jdbc/execute! connecter ["SELECT * FROM task where id = 7"])
;; (njs/find-by-keys connecter :task {:id 2, :user_id 1})

;; (jdbc/execute! connecter ["SELECT * FROM user_device"])
;; (second (jdbc/execute! connecter ["SELECT * FROM user_token where user_id = 1"]))

;; (njs/insert! connecter :user_device
;;              {:user_id 1
;;               :created_at (sql-now)
;;               :endpoint "https://fcm.googleapis.com/fcm/send/f8ZrPSJxQVk:APA91bF2DMtsF8Wyhogehogehoge0gAU5b2GcdUOzO1eR8jyX3UbpfmYC-oFqZE0VBr658gJ0MvMBUKoBmp3h00VWO2a2E2dBH8aD3QcbYAgrEaJN914jPPwWCFKjzCGjMe8PNbarbarU"
;;               :auth "cfoobarjl529A_X-bw"
;;               :p256dh "BPT8zHapyg8fbcGJqzNlKXj0gTdSmKOcVn4V7rwaceFwf2h_ZXqiQtLHya7KeMPd3YXTrPoSt39AqtCTrzWX45o"})

