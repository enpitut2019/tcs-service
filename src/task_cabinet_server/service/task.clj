(ns task-cabinet-server.service.task
  (:require
   [task-cabinet-server.service.user-token :as token]
   [task-cabinet-server.spec.user-token :as tokens]
   [task-cabinet-server.spec.task :as tasks]
   [task-cabinet-server.spec.users  :as users]
   [task-cabinet-server.service.utils :as utils]
   [task-cabinet-server.Boundary.task :as tsql]
   [clojure.spec.alpha :as s]
   [clojure.walk :as w]))

;;; handler & specs ;;;
(s/def ::create-task
  (s/keys :req-un [::tasks/name ::tasks/deadline ::tasks/estimate]
          :opt-un [::tasks/description ::tasks/category]))
(defn create-task-handler
  "create a task
  returns:
  - 400 invalid args
  - 403 forbidden"
  [{:keys [parameters headers path-params db]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        user-id (-> path-params :user-id Integer/parseInt)
        {:keys [body]} parameters]
    (print (s/explain ::create-task body))
    (if-not (and (s/valid? ::create-task body) (s/valid? ::tokens/token authorization)
                 (s/valid? ::users/id user-id))
      {:status 400}
      (if (-> (token/check-token-exists? db user-id authorization) count zero?)
        {:status 403}
        {:status 201
         :body {:result
                (into {} (remove (fn [[k v]] (nil? v)))
                      (tsql/create-task db (assoc body :user_id user-id)))}}))))

(defn get-task-info-handler
  "get task information
  returns:
  - 400 invalid args
  - 403 forbidden
  - 404 not found"
  [{:keys [headers path-params db]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        user-id (-> path-params :user-id Integer/parseInt)
        id (-> path-params :id Integer/parseInt)]
    (if-not (and (s/valid? ::users/id user-id) (s/valid? ::tasks/id id)
                 (s/valid? ::tokens/token authorization))
      {:status 400}
      (if (zero? (count (token/check-token-exists? db user-id authorization)))
        {:status 403}
        (if-let [task (tsql/find-task db {:id id :user_id user-id})]
          {:status 201
           :body {:result task}}
          {:status 404})))))

(defn delete-task-handler
  "delete a task
  returns:
  - 400 invalid args
  - 403 forbidden
  - 404 not found"
  [{:keys [path-params headers db]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        user-id (-> path-params :user-id Integer/parseInt)
        id (-> path-params :id Integer/parseInt)]
    (if-not (and (s/valid? ::users/id user-id) (s/valid? ::tasks/id id)
                 (s/valid? ::tokens/token authorization))
      {:status 400}
      (if  (zero? (count (token/check-token-exists? db user-id authorization)))
        {:status 403}
        (let
            [res (tsql/delete-task db {:id id :user_id user_id})]
          (println "Res " res)
          (if (zero? res)
                 {:status 404}
                 {:status 200}))))))

(s/def ::update-task
  (s/keys :req-un [::tasks/name ::tasks/deadline ::tasks/estimate]
          :opt-un [::tasks/description ::tasks/category]))
(defn update-task-handler
  "update a task
  returns:
  - 400 invalid args
  - 403 forbidden
  - 404 not found"
  [{:keys [path-params headers parameters db]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        user-id (-> path-params :user-id Integer/parseInt)
        id (-> path-params :id Integer/parseInt)
        {:keys [body]} parameters]
    (if-not (and (s/valid? ::users/id user-id) (s/valid? ::tasks/id id)
                 (s/valid? ::tokens/token authorization)
                 (s/valid? ::update-task body))
      {:status 400}
      (if (-> (token/check-token-exists? db user-id authorization) count zero?)
        {:status 403}
        (if (or (:is_deleted (tsql/get-task db :id id))
                (zero? (tsql/update-task db body  {:id id :user_id user_id})))
          {:status 404}
          {:status 200
           :body {:result
                  (select-keys (tsql/get-task db :id id)
                               [:name :deadline :estimate :created_at :updated_at :description :category])}})))))

(defn complete-task-handler
  "complete task
    returns:
  - 400 invalid args
  - 403 forbidden
  - 404 not found"
  [{:keys [path-params headers db]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        id (-> path-params :id Integer/parseInt)
        user-id (-> path-params :user-id Integer/parseInt)]
    (if-not (and (s/valid? ::users/id user-id) (s/valid? ::tasks/id id)
                 (s/valid? ::tokens/token authorization))
      {:status 400 :message
       {:token (s/explain ::tokens/token authorization)
        :users_id (s/explain ::users/id user_id)
        :task_id (s/explain ::tasks/id id)}}
      (if (-> (token/check-token-exists? db user-id authorization) count zero?)
        {:status 403}
        (let [task (tsql/get-task db :id id)]
          (if (or (:is_deleted task)
                  (:finished_at task))
            {:status 404 :message "resource not found"}
            (do
              (if (zero? (tsql/complete-task db {:id id :user_id id}))
                {:status 404 :message "resource not found"}
                {:status 200}))))))))

(s/def ::all boolean?)
(defn get-list-task-handler
  [{:keys [parameters headers path-params db]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        user-id (-> path-params :user-id Integer/parseInt)
        {{:keys [all]} :query} parameters]
    (if-not (and (s/valid? ::users/id user-id)
                 (s/valid? ::tokens/token authorization)
                 (s/valid? ::all all))
      {:status 400}
      (if-not (token/check-token-exists? db  user-id authorization)
        {:status 403}
        {:status 200
         :body {:result
                (tsql/get-list-task db user-id all)}}))))
