(ns task-cabinet-server.Boundary.users
  (:require [next.jdbc :as jdbc]
            [honeysql.core :as sql]
            [honeysql.helpers :as h]
            [task-cabinet-server.Boundary.utils.util :as utils]
            [task-cabinet-server.Boundary.utils.sql :as s]))

(defprotocol Users
  (get-users [db])
  (get-user [db k v])
  (create-user [db user])
  (update-user [db m idm])
  (delete-user [db idm])
  (erase-user[db  user]))

(extend-protocol Users
  task_cabinet_server.Boundary.utils.sql.Boundary
  (get-users [{:keys [spec]}]
    (with-open [conn (jdbc/get-connection (:datasource spec))]
     (jdbc/execute! conn ["SELECT * FROM users"])))
  (get-user [{:keys [spec]} k v]
    (let [res (cond->
               (utils/get-by-id spec :users  k v)
             #(:created_at %)  (update  :created_at utils/sql-to-long)
             #(:updated_at %) (update :updated_at utils/sql-to-long))
          res (into {} (remove (fn [[k v]] (nil? v)) res))]
      res))
  (create-user [{:keys [spec]} user]
    (utils/insert! spec :users user))
  (update-user [{:keys [spec]} m idm]
    (utils/update! spec :users m idm))
  (delete-user [{:keys [spec]} idm]
    (let [email (:email (utils/get-by-id spec :users :id (:id idm)))]
      (utils/update! spec :users {:is_deleted true :email (str email "-"(:id idm))} idm))))

;; (extend-protocol Users
;;   task_cabinet_server.Boundary.utils.sql.Boundary
;;   (get-users [{:keys [spec]}]
;;     (let [scl (sql/format
;;                {:select [:users/id :users/name :users/email]
;;                 :from [:users]})]
;;       (utils/run-sql spec scl false)))
;;   (get-user [{:keys [spec]} {:keys [users/email]}]
;;     (let [scl (sql/format
;;                {:select [:*]
;;                 :from [:users]
;;                 :where  [:= :users/email email]})
;;           res (utils/run-sql spec scl true)]
;;       res))
;;   (create-user [{:keys [spec]} user]
;;     (let [scl (sql/format
;;                (-> (h/insert-into :users)
;;                    (h/values [user])))]
;;       (utils/run-sql spec scl true)))
;;   (update-password [{:keys [spec]} {:keys [users/id users/password]}]
;;     (let [scl (sql/format
;;                (-> (h/update :users)
;;                    (h/sset
;;                     {:users/password password
;;                      :users/updated_at (utils/sql-now)})
;;                    (h/where [:= :users/id id])))]
;;       (utils/run-sql spec scl true)))
;;   (delete-user [{:keys [spec]} {:keys [users/id]}]
;;     (let [scl (sql/format
;;                (-> (h/update :users)
;;                    (h/sset
;;                     {:users/is_deleted true
;;                      :users/updated_at (utils/sql-now)})
;;                    (h/where [:= :users/id id])))]
;;       (utils/run-sql spec scl true)))
;;   (erase-user [{:keys [spec]} {:keys [users/id]}]
;;     (let [scl (sql/format
;;                (-> (h/delete-from :users)
;;                    (h/where [:= :users/id id])))]
;;       (utils/run-sql spec scl true))))


;; for debug
 ;; (defonce inst (task-cabinet-server.Boundary.utils.sql/->Boundary {:datasource (hikari-cp.core/make-datasource {:jdbc-url (environ.core/env :database-url)})}))

;; (get-users inst)



;; ;; found return all columns
;;(get-user inst
;;          {:users/email  "test@gmail.com"})

;; ;; not found return nil
;; (get-user inst
;;           {:users/email  "test2@gmail.com"})


;; (defonce test-creation
;;   (create-user inst {:users/name "meguru"
;;                      :users/email "test@gmail.com"
;;                      :users/password (utils/hash-password "emacs")}))

;; conflicts raise error
;; (create-user inst {:users/name "meguru"
;;                    :users/email "test@gmail.com"
;;                    :users/password (utils/hash-password "emacs")})

;; update success return 1
;; update fail  return 0
;; (:next.jdbc/update-count
;;  (update-password inst
;;                   {:users/id 2 :users/password (utils/hash-password "test")}))

;; (get-users inst)


;; update success return 1
;; update fail return 0
;; (:next.jdbc/update-count
;;  (delete-user inst {:users/id 2}))

;; success 1
;; fail 0
;; (erase-user inst {:users/id 1})

