(ns task-cabinet-server.Boundary.user-token
  (:require [next.jdbc :as jdbc]
            [honeysql.core :as sql]
            [honeysql.helpers :as h]
            [task-cabinet-server.Boundary.utils.util :as utils]
            [task-cabinet-server.Boundary.utils.sql :as s]))

(defprotocol User-Token
  (get-user-tokens [db user])
  (create-user-token [db user-token])
  (erase-user-token [db user-token]))

(extend-protocol User-Token
    task_cabinet_server.Boundary.utils.sql.Boundary
  (get-user-tokens [{:keys [spec]} {:keys [users/id]}]
    (let [scl (-> (h/select :user_token/user_id :user_token/token)
                  (h/where [:= :user_token/user_id id]))
          scl (sql/format scl)]
      (utils/run-sql spec scl false))))
