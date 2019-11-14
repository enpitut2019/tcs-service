(ns task-cabinet-server.Boundary.task
  (:require [next.jdbc :as jdbc]
            [honeysql.core :as sql]
            [honeysql.helpers :as h]
            [task-cabinet-server.Boundary.utils.util :as utils]
            [task-cabinet-server.Boundary.utils.sql :as s]))


(defprotocol Task
  (get-tasks [db user opt])
  (get-task [db task])
  (get-task-by-category [db task user])
  (create-task [db task user])
  (update-task [db task])
  (delete-task [db task])
  (complete-task [db task])
  (erase-task [db task]))

(extend-protocol Task
  task_cabinet_server.Boundary.utils.sql.Boundary
  (get-tasks [{:keys [spec]} {:keys [users/id]} {:keys [deleted-task? finished-task? description?]}]
    (let [scl
          (-> (h/select :task/name :task/category :task/deadline :task/estimate :task/finished_at :task/created_at :task/updated_at)
              (h/from :task)
              (h/where [:= :task/user_id id]))
          scl (if deleted-task? scl (-> scl (h/merge-where [:= :task/is_deleted false]) (h/merge-select :task/is_deleted)))
          scl (if finished-task? scl (-> scl (h/merge-where  [:= :task/finished-at nil]) (h/merge-select :task/finished_at)))
          scl (if description? scl (-> scl (h/merge-select :task/description)))
          scl (sql/format scl)]
      (utils/run-sql spec scl false)))
  (get-task [{:keys [spec]} {:keys [task/id]}]
    (let [scl
          (-> (h/select :*)
              (h/from :task)
              (h/where [:= :task/id id]))
          scl (sql/format scl)]
      (utils/run-sql spec scl true)))
  (get-task-by-category [{:keys [spec]} {:keys [task/category]} {:keys [user/id]}]
    (let [scl
          (-> (h/select :*)
              (h/from :task)
              (h/where [:and [:= :user/id id] [:= :task/category category]]))
          scl (-> scl sql/format)]
      (utils/run-sql spec scl false)))
  (create-task [{:keys [spec]} task]
    (let [scl
          (-> (h/insert-into :task)
              (h/values [task]))
          scl (sql/format scl)]
      (utils/run-sql spec scl true)))
  (update-task [{:keys [spec]} task]
    (let [scl
          (-> (h/update :task)
              (h/sset task)
              (h/where [:= :task/id (:task/id task)]))
          scl (sql/format scl)]
      (utils/run-sql spec scl true)))
  (delete-task [{:keys [spec]} {:keys [task/id]}]
    (let [scl
          (-> (h/update :task)
              (h/sset {:task/is_deleted true
                       :task/updated_at (utils/sql-now)})
              (h/where [:= :task/id id]))
          scl (-> scl sql/format)]
      (utils/run-sql spec scl true)))
  (complete-task [{:keys [spec]} {:keys [task/id]}]
    (let [scl
          (-> (h/update :task)
              (h/sset {:task/finished-at (utils/sql-now)
                       :task/updated_at (utils/sql-now)}))
          scl (-> scl sql/format) ]
      (utils/run-sql spec scl true)))
  (erase-task [{:keys [spec]} {:keys [task/id]}]
    (let [scl
          (-> (h/delete :task)
              (h/where [:= :task/id id]))
          scl (sql/format scl)]
      (utils/run-sql spec scl true))))



;; for debug
;; (clojure.spec.alpha/def ::t string?)
;; (def x {::t 123})
;; (clojure.spec.alpha/conform ::t x)

;; (get-tasks task-cabinet-server.Boundary.users/inst {:users/id 1} {:deleted-task? false :finished-task? false})
;; (get-task task-cabinet-server.Boundary.users/inst {:task/id 1} )
;; (get-task-by-category task-cabinet-server.Boundary.users/inst {:task/category "test"} {:user/id 1})
