(ns task-cabinet-server.Boundary.user-device
  (:require [next.jdbc :as jdbc]
            [honeysql.core :as sql]
            [honeysql.helpers :as h]
            [task-cabinet-server.Boundary.utils.util :as utils]
            [task-cabinet-server.Boundary.utils.sql :as s]))
;; check-device-exists? user_id authorization auth p256dh
;; add-token user_id endpoint auth p256dh

(defprotocol User-Device
  (check-device-exists? [db m])
  (remove-all-device [db user-id])
  (add-device [db m])
  (remove-device [db m]))

(extend-protocol User-Device
  task_cabinet_server.Boundary.utils.sql.Boundary
  (check-device-exists? [{:keys [spec]} m]
    (utils/find-by-m spec :user_device m))
  (add-device [{:keys [spec]} m]
    (->  (utils/insert! spec :user_device (assoc m :created_at (utils/sql-now)))
         (dissoc :user_id)
         (dissoc :created_at)
         (dissoc :id)))
  (remove-all-device [{:keys [spec]} user-id]
    (utils/delete! spec :user_device {:user_id user-id}))
  (remove-device [{:keys [spec]} m]
    (utils/delete! spec :user_device m)))

;; (extend-protocol User-Device
;;   task_cabinet_server.Boundary.utils.sql.Boundary
;;   (get-user-devices [{:keys [spec]} {:keys [:users/user_id]}]
;;     (let [scl (-> (h/select :user_device/endpoint :user_device/auth :user_device/p256dh :user_device/created_at)
;;                   (h/from :user_device)
;;                   (h/where [:= :user_device/user_id user_id]))
;;           scl (sql/format scl)]
;;       (utils/run-sql spec scl false)))
;;   (create-user-device [{:keys [spec]} user-device]
;;     (let [scl (-> (h/insert-into :user_device)
;;                   (h/values [user-device]))
;;           scl (sql/format scl)]
;;       (utils/run-sql spec scl true)))
;;   (erase-user-device [{:keys [spec]} {:keys [user_device/id]}]
;;     (let [scl (-> (h/delete :user_device)
;;                   (h/where [:= :user_device/id id]))
;;           scl (sql/format scl)]
;;       (utils/run-sql spec scl true))))
