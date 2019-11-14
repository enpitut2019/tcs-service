(ns task-cabinet-server.Boundary.user-token
  (:require [next.jdbc :as jdbc]
            [honeysql.core :as sql]
            [honeysql.helpers :as h]
            [task-cabinet-server.Boundary.utils.util :as utils]
            [task-cabinet-server.Boundary.utils.sql :as s]))

(defprotocol User-Token
  (add-token [db user-id token])
  (get-token [db user-id token])
  (delete-token [db user-id token])
  (delete-all-token [db user-id]))

(extend-protocol User-Token
  task_cabinet_server.Boundary.utils.sql.Boundary
  (add-token [{:keys [spec]} user-id token]
    (utils/insert! spec :user_token {:user_id user-id :token token}))
  (get-token [{:keys [spec]} user-id token]
    (utils/find-by-m spec :user_token {:user_id user-id :token token}))
  (delete-token [{:keys [spec]} user-id token]
    (utils/delete! spec :user_token {:user_id user-id :token token}))
  (delete-all-token [{:keys [spec]} user-id]
    (utils/delete! spec :user_token {:user_id user-id})))
;; (extend-protocol User-Token
;;     task_cabinet_server.Boundary.utils.sql.Boundary
;;   (get-user-tokens [{:keys [spec]} {:keys [users/id]}]
;;     (let [scl (-> (h/select :user_token/user_id :user_token/token)
;;                   (h/where [:= :user_token/user_id id]))
;;           scl (sql/format scl)]
;;       (utils/run-sql spec scl false))))
