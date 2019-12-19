(ns task-cabinet-server.service.user-device
  (:require
   [task-cabinet-server.spec.user-device :as devices]
   [task-cabinet-server.spec.user-token :as tokens]
   [task-cabinet-server.spec.users :as users]
   [task-cabinet-server.service.user-token :as token]
   [task-cabinet-server.service.utils :as utils]
   [task-cabinet-server.Boundary.users :as usql]
   [task-cabinet-server.Boundary.user-device :as udsql]
   [clojure.walk :as w]
   [clojure.spec.alpha :as s]))

;;; debug utils ;;;
(defn add-token
  "m
  {:user-id id
   :endpoint endpoint
   :auth auth
   :p256dh p256dh}
  "
  [db m]
  ;; (deviceb/add-token m)
  {:endpoint (:endpoint m)
   :keys {:auth (:auth m)
          :p256dh (:p256dh m)}})

(defn check-device-exists? [db m]
  true)

(defn remove-all-device [db user-id]
  2)

(defn remove-device [db m]
  1)


(defn conform-user-with-id
  "check user's information is valid
  args:
  - id : id addrfess
  - password : un-hashed password
  returns nil or not nil"
  [db user-id password]
  (let [candidate (usql/get-user db :id user-id)]
    (if (:is_deleted candidate)
      nil
      (utils/check-password candidate password))))

;;; handler & specs ;;;
(defn check-device-handler
  "check devices
  returns:
  - 200 ok exist
  - 409 conflict
  - 400 invalid args"
  [{:keys [parameters headers path-params db]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        user-id (-> path-params :user-id Integer/parseInt)
        {{:keys [endpoint keys]} :body} parameters
        {:keys [auth p256dh]} keys]
    (if-not (and (s/valid? ::tokens/token authorization)
                 (s/valid? ::devices/endpoint endpoint)
                 (s/valid? ::devices/auth auth)
                 (s/valid? ::devices/p256dh p256dh)
                 (s/valid? ::users/id user-id))
      {:status 400}
      (if (-> (token/check-token-exists? db user-id authorization) count zero?)
        {:status 403}
        (if-not (-> (udsql/check-device-exists? db
                                                {:user_id user-id :endpoint endpoint :auth auth :p256dh p256dh})
                    count zero?)
          {:status 200 :body {:is_exist 1}}
          {:status 200 :body {:is_exist 0}})))))

(defn add-device-handler
  "add devices
  returns:
  - 409 conflict
  - 400 invalid args"
  [{:keys [parameters headers path-params db]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        user-id (-> path-params :user-id Integer/parseInt)
        {{:keys [endpoint keys]} :body} parameters
        {:keys [auth p256dh]} keys]
    (if-not (and (s/valid? ::tokens/token authorization)
                 (s/valid? ::devices/endpoint endpoint)
                 (s/valid? ::devices/auth auth)
                 (s/valid? ::devices/p256dh p256dh)
                 (s/valid? ::users/id user-id))
      {:status 400}
      (if (-> (token/check-token-exists? db user-id authorization) count zero?)
        {:status 403}
        (if-not (-> (udsql/check-device-exists? db
                                                {:user_id user-id :endpoint endpoint :auth auth :p256dh p256dh})
                    count zero?)
          {:status 409}
          (let [res (udsql/add-device  db {:user_id user-id :endpoint endpoint :auth auth :p256dh p256dh})
                res (assoc res :keys {})
                res (assoc-in res [:keys :auth] (:auth res))
                res (assoc-in res [:keys :p256dh] (:p256dh res))
                res (dissoc res :auth)
                res (dissoc res :p256dh)]
            {:status 201
             :body {:result res}}))))))

(defn remove-device-handler
  "
  returns
  - 400 invalid args
  - 403 forbidden
  - 404 resource not found
  "
  [{:keys [parameters headers path-params db]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        user-id (-> path-params :user-id Integer/parseInt)
        {{:keys [endpoint keys]} :body} parameters
        {:keys [auth p256dh]} keys]
    (if-not (and (s/valid? ::tokens/token authorization)
                 (s/valid? ::devices/endpoint endpoint)
                 (s/valid? ::devices/auth auth)
                 (s/valid? ::devices/p256dh p256dh))
      {:status 400}
      (if (-> (token/check-token-exists?  db user-id authorization) count zero?)
        {:status 403}
        (if  (udsql/remove-device db
                                  {:user_id user-id :endpoint endpoint
                                   :auth auth :p256dh p256dh})
            {:status 200}
            {:status 404})))))

(defn remove-all-device-handler
  "returns
  - 403 forbidden
  - 401 unauthorized
  "
  [{:keys [parameters headers path-params db]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        user-id (-> path-params :user-id Integer/parseInt)
        password (-> parameters :body :password)]
    (if (-> (token/check-token-exists? db user-id authorization) count zero?)
      {:status 403}
      (if (conform-user-with-id db user-id password)
        (do (udsql/remove-all-device db user-id)
            {:status 200})
        {:status 401}))))
