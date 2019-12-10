(ns task-cabinet-server.Boundary.task
  (:require [next.jdbc :as jdbc]
            [honeysql.core :as sql]
            [honeysql.helpers :as h]
            [task-cabinet-server.Boundary.utils.util :as utils]
            [task-cabinet-server.Boundary.utils.sql :as s]))


(defprotocol Task
  (get-task [db k v])
  (find-task [db m])
  (create-task [db task])
  (delete-task [db m])
  (update-task [db task idm])
  (complete-task [db idm])
  (get-list-task [db user-id all]))

(extend-protocol Task
  task_cabinet_server.Boundary.utils.sql.Boundary
  (get-task [{:keys [spec]} k v]
    (let [res (cond->
                  (utils/get-by-id spec :task k v)
                #(:deadline %) (update :deadline utils/sql-to-long)
                #(:created_at %) (update :created_at utils/sql-to-long)
                #(:updated_at %) (update :updated_at utils/sql-to-long)
                #(:finished_at %) (update :finished_at utils/sql-to-long))
          res (into {} (remove (fn [[k v]] (nil? v))) res)
          res (dissoc res :user_id)]
      res))
  (find-task [{:keys [spec]} m]
    (let [res (cond->
                  (first (utils/find-by-m spec :task m))
                #(:deadline %) (update :deadline utils/sql-to-long)
                #(:created_at %) (update :created_at utils/sql-to-long)
                #(:updated_at %) (update :updated_at utils/sql-to-long)
                #(:finished_at %) (update :finished_at utils/sql-to-long))
          res (into {} (remove (fn [[k v]] (nil? v))) res)
          res (dissoc res :user_id)]
      (if (empty? res)
        nil
        res)))
  (create-task [{:keys [spec]} task]
    (->
     (utils/insert! spec :task
                    (-> task
                        (update :deadline utils/long-to-sql)
                        (assoc :is_deleted false)))
     (update :created_at utils/sql-to-long)
     (update :deadline utils/sql-to-long)
     (dissoc :user_id)
     (dissoc :is_deleted))) ;; TODO UPDATE
  (delete-task [{:keys [spec]} m]
    (utils/update! spec :task
                   {:is_deleted true :updated_at (utils/sql-now)} m))
  (update-task [{:keys [spec]} task idm]
    (let [task (cond-> task
                 #(:deadline %) (update :deadline utils/long-to-sql)
                 #(:created_at %) (update :created_at utils/long-to-sql)
                 #(:updated_at %) (update :updated_at utils/long-to-sql)
                 #(:finished_at %) (update :finished_at utils/long-to-sql))]
      (utils/update! spec :task task idm)))
  (complete-task [{:keys [spec]} idm]
    (let [res (utils/update! spec :task {:finished_at (utils/sql-now)} idm)]
      res))
  (get-list-task [{:keys [spec]} user-id all]
    (let [res (if all
                (utils/find-by-m spec :task {:user_id user-id})
                (filter #(not (:is_deleted %)) (utils/find-by-m spec :task {:user_id user-id})))
          res (map  #(dissoc % :user_id) res)
          res (map (fn [task] (cond-> task
                                :finished_at (update :finished_at utils/sql-to-long)
                                :deadline (update :deadline utils/sql-to-long)
                                :created_at (update :created_at utils/sql-to-long)
                                :updated_at (update :updated_at utils/sql-to-long))) res)]
      (map #(into {} (remove (fn [[k v]] (nil? v)) %)) res))))


;; (extend-protocol Task
;;   task_cabinet_server.Boundary.utils.sql.Boundary
;;   (get-tasks [{:keys [spec]} {:keys [users/id]} {:keys [deleted-task? finished-task? description?]}]
;;     (let [scl
;;           (-> (h/select :task/name :task/category :task/deadline :task/estimate :task/finished_at :task/created_at :task/updated_at)
;;               (h/from :task)
;;               (h/where [:= :task/user_id id]))
;;           scl (if deleted-task? scl (-> scl (h/merge-where [:= :task/is_deleted false]) (h/merge-select :task/is_deleted)))
;;           scl (if finished-task? scl (-> scl (h/merge-where  [:= :task/finished-at nil]) (h/merge-select :task/finished_at)))
;;           scl (if description? scl (-> scl (h/merge-select :task/description)))
;;           scl (sql/format scl)]
;;       (utils/run-sql spec scl false)))
;;   (get-task [{:keys [spec]} {:keys [task/id]}]
;;     (let [scl
;;           (-> (h/select :*)
;;               (h/from :task)
;;               (h/where [:= :task/id id]))
;;           scl (sql/format scl)]
;;       (utils/run-sql spec scl true)))
;;   (get-task-by-category [{:keys [spec]} {:keys [task/category]} {:keys [user/id]}]
;;     (let [scl
;;           (-> (h/select :*)
;;               (h/from :task)
;;               (h/where [:and [:= :user/id id] [:= :task/category category]]))
;;           scl (-> scl sql/format)]
;;       (utils/run-sql spec scl false)))
;;   (create-task [{:keys [spec]} task]
;;     (let [scl
;;           (-> (h/insert-into :task)
;;               (h/values [task]))
;;           scl (sql/format scl)]
;;       (utils/run-sql spec scl true)))
;;   (update-task [{:keys [spec]} task]
;;     (let [scl
;;           (-> (h/update :task)
;;               (h/sset task)
;;               (h/where [:= :task/id (:task/id task)]))
;;           scl (sql/format scl)]
;;       (utils/run-sql spec scl true)))
;;   (delete-task [{:keys [spec]} {:keys [task/id]}]
;;     (let [scl
;;           (-> (h/update :task)
;;               (h/sset {:task/is_deleted true
;;                        :task/updated_at (utils/sql-now)})
;;               (h/where [:= :task/id id]))
;;           scl (-> scl sql/format)]
;;       (utils/run-sql spec scl true)))
;;   (complete-task [{:keys [spec]} {:keys [task/id]}]
;;     (let [scl
;;           (-> (h/update :task)
;;               (h/sset {:task/finished-at (utils/sql-now)
;;                        :task/updated_at (utils/sql-now)}))
;;           scl (-> scl sql/format) ]
;;       (utils/run-sql spec scl true)))
;;   (erase-task [{:keys [spec]} {:keys [task/id]}]
;;     (let [scl
;;           (-> (h/delete :task)
;;               (h/where [:= :task/id id]))
;;           scl (sql/format scl)]
;;       (utils/run-sql spec scl true))))



;; for debug
;; (clojure.spec.alpha/def ::t string?)
;; (def x {::t 123})
;; (clojure.spec.alpha/conform ::t x)

;; (get-tasks task-cabinet-server.Boundary.users/inst {:users/id 1} {:deleted-task? false :finished-task? false})
;; (get-task task-cabinet-server.Boundary.users/inst {:task/id 1} )
;; (get-task-by-category task-cabinet-server.Boundary.users/inst {:task/category "test"} {:user/id 1})
