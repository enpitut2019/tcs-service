(ns task-cabinet-server.Boundary.user-device
  (:require [next.jdbc :as jdbc]
            [honeysql.core :as sql]
            [honeysql.helpers :as h]
            [task-cabinet-server.Boundary.utils.util :as utils]
            [task-cabinet-server.Boundary.utils.sql :as s]))

(defprotocol User-Device
  (get-user-devices [db user])
  (create-user-device [db user-device])
  (erase-user-device [db user-device]))

(extend-protocol User-Device
  task_cabinet_server.Boundary.utils.sql.Boundary
  (get-user-devices [{:keys [spec]} {:keys [:users/user_id]}]
    (let [scl (-> (h/select :user_device/endpoint :user_device/auth :user_device/p256dh :user_device/created_at)
                  (h/from :user_device)
                  (h/where [:= :user_device/user_id user_id]))
          scl (sql/format scl)]
      (utils/run-sql spec scl false)))
  (create-user-device [{:keys [spec]} user-device]
    (let [scl (-> (h/insert-into :user_device)
                  (h/values [user-device]))
          scl (sql/format scl)]
      (utils/run-sql spec scl true)))
  (erase-user-device [{:keys [spec]} {:keys [user_device/id]}]
    (let [scl (-> (h/delete :user_device)
                  (h/where [:= :user_device/id id]))
          scl (sql/format scl)]
      (utils/run-sql spec scl true))))
