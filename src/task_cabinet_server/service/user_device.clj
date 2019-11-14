(ns task-cabinet-server.service.user-device
  (:require
   [task-cabinet-server.spec.user-device :as devices]
   [task-cabinet-server.spec.user-token :as tokens]
   [task-cabinet-server.service.user-token :as token]
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

(defn conform-user-with-id [db id password]
  true)
(defn remove-all-device [db id]
  2)

(defn remove-device! [db m]
  1)

;;; handler & specs ;;;
(defn add-device-handler
  "add devices
  returns:
  - 409 conflict
  - 400 invalid args"
  [db]
  (fn
    [{:keys [parameters headers path-params]}]
    (let [{:keys [authorization]} (w/keywordize-keys headers)
          user-id (-> path-params :user-id Integer/parseInt)
          {{:keys [endpoint keys]} :body} parameters
          {:keys [auth p256dh]} keys]
      (if-not (and (s/valid? ::tokens/token authorization)
                   (s/valid? ::devices/endpoint endpoint)
                   (s/valid? ::devices/auth auth)
                   (s/valid? ::devices/p256dh p256dh))
        {:status 400}
        (if-not (token/check-token-exists? user-id authorization)
          {:status 403}
          (if (check-device-exists? {:user-id user-id :authorization authorization :auth auth :p256dh p256dh})
            {:status 409}
            {:status 201
             :body {:result (add-token {:user-id user-id :endpoint endpoint :auth auth :p256dh p256dh})}}))))))

(defn remove-device-handler
  "
  returns
  - 400 invalid args
  - 403 forbidden
  - 404 resource not found
  "
  [db]
  (fn  [{:keys [parameters headers path-params]}]
    (let [{:keys [authorization]} (w/keywordize-keys headers)
          user-id (-> path-params :user-id Integer/parseInt)
          {{:keys [endpoint keys]} :body} parameters
          {:keys [auth p256dh]} keys]
      (if-not (and (s/valid? ::tokens/token authorization)
                   (s/valid? ::devices/endpoint endpoint)
                   (s/valid? ::devices/auth auth)
                   (s/valid? ::devices/p256dh p256dh))
        {:status 400}
        (if-not (token/check-token-exists? user-id authorization)
          {:status 403}
          (if (zero? (remove-device! {:user-id user-id :authorization authorization :auth auth :p256dh p256dh}))
            {:status 200}
            {:status 404}))))))

(defn remove-all-device-handler
  "returns
  - 403 forbidden
  - 401 unauthorized
  "
  [db]
  (fn [{:keys [parameters headers path-params]}]
    (let [{:keys [authorization]} (w/keywordize-keys headers)
          user-id (-> path-params :user-id Integer/parseInt)
          password (-> parameters :body :password)]
      (if-not (token/check-token-exists? user-id authorization)
        {:status 403}
        (if (conform-user-with-id user-id password)
          (do
            (remove-all-device user-id)
            {:status 200})
          {:status 401})))))
