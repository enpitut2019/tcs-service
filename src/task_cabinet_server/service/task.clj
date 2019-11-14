(ns task-cabinet-server.service.task
  (:require
   [task-cabinet-server.service.user-token :as token]
   [task-cabinet-server.spec.user-token :as tokens]
   [task-cabinet-server.spec.task :as tasks]
   [task-cabinet-server.spec.users  :as users]
   [task-cabinet-server.service.utils :as utils]
   [clojure.spec.alpha :as s]
   [clojure.walk :as w]))

;;; debug functions ;;;
(def tmp-task-list
  [{:id 1
    :name "Implement Server"
    :deadline 1573493099290
    :estimate 40
    :created_at 1572566411400}
   {:id 2
    :name "Implement WebPush"
    :deadline 1572567452000
    :estimate 80
    :created_at 1572566431400
    :finished_at 1572567432000
    :category "server"}
   {:id 3
    :name "Implement Authorization"
    :deadline 1572566451400
    :estimate 60
    :created_at 1572566441400
    :category "server"}
   {:id 4
    :name "Re: check database structure"
    :deadline 1572566952000
    :estimate 100
    :created_at 1572566411500
    :finished_at 1572567431000}])

(defn get-task
  "m has key :id"
  [db k v]
  (first (filter
     #(= (:id % ) v) tmp-task-list)))

(defn create-task
  [db m]
  (assoc (assoc m :created_at (utils/now-long)) :id 1))

(defn delete-task
  "deleted 1
not found 0"
  [db id]
  1)

(defn update-task
  [db m]
  1)

(defn complete-task
  "m
  {:id id}"
  [db m]
  1)

(defn get-list-task
  "m
  {:user-id user-id}"
  [db m]
  tmp-task-list)
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
    (if-not (and (s/valid? ::create-task body) (s/valid? ::tokens/token authorization)
                 (s/valid? ::users/id user-id))
      {:status 400}
      (if-not (token/check-token-exists? db user-id authorization)
        {:status 403}
        {:status 201
         :body {:result (create-task db body)}}))))

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
    (if-not (and (s/valid? ::users/id user-id) (s/valid? ::tasks/id id) (s/valid? ::tokens/token authorization))
      {:status 400}
      (if (zero? (count (token/check-token-exists? db user-id authorization)))
        {:status 403}
        (if-let [task (get-task db :id id)]
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
    (if-not (and (s/valid? ::users/id user-id) (s/valid? ::tasks/id id) (s/valid? ::tokens/token authorization))
      {:status 400}
      (if  (zero? (count (token/check-token-exists? db user-id authorization)))
        {:status 403}
        (if (zero? (delete-task db id))
          {:status 404}
          {:status 200})))))

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
      (do (s/explain ::update-task body)
          {:status 400})
      (if (-> (token/check-token-exists? db user-id authorization) count zero?)
        {:status 403}
        (if (zero? (update-task db (assoc body :id id)))
          {:status 404}
          {:status 200
           :body {:result
                  (select-keys (get-task db :id id)
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
      {:status 400}
      (if (-> (token/check-token-exists? db user-id authorization) count zero?)
        {:status 403}
        (if (zero? (complete-task db {:id id}))
          {:status 404}
          {:status 200})))))

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
                (get-list-task db {:user-id user-id})}}))))
