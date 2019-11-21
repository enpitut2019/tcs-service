(ns task-cabinet-server.service.users
  (:require
   [task-cabinet-server.service.user-token :as token]
   [task-cabinet-server.spec.users :as users]
   [task-cabinet-server.spec.user-token :as tokens]
   [task-cabinet-server.service.utils :as utils]
   [task-cabinet-server.Boundary.users :as usql]
   [task-cabinet-server.Boundary.user-token :as utsql]
   [clojure.spec.alpha :as s]
   [clojure.walk :as w]))

;;; utils ;;;
(defn already-exists?
  "check user is exists in database
  args:
  - email: email address
  returns:
  user or nil"
  [db email]
  (let [res(usql/get-user db  :email email)]
    (println "already exists?" res)
    (if (empty? res) nil
        res)))

(defn conform-user
  "check user's information is valid
  args:
  - email: email address
  - password: un-hashed-password
  returns:
  user or nil"
  [db email password]
  (let [candidate (usql/get-user db :email email)]
    (if (:is_deleted candidate)
      nil
     (utils/check-password candidate password))))

;;; handler & spec ;;;
(s/def ::user-login-body (s/keys :req-un [::users/email ::users/password]))
(defn login-handler
  "login handler
  returns:
  - 400 invalid args
  - 401 unauthorized"
  [{:keys [parameters db]}]
  (let [{{:keys [email password] :as body} :body} parameters]
      (if-not (s/valid? ::user-login-body body) {:status 400}
           (if-let [user-info (conform-user db email password)]
             {:status 200
              :body
              {:result
               {:id (-> user-info :id)
                :token (token/garanteed-random-token (:id user-info) db)}}}
             {:status 401}))))

(s/def ::user-create-body (s/keys :req-un [::users/name ::users/email ::users/password]))
(defn create-user-handler
  "create user handler
  returns:
  - 400 invalid args
  - 409 conflict"
  [{:keys [parameters db] :as params}]
  (let [{{:keys [name password email] :as body} :body}   parameters]
    (if-not (s/valid? ::user-create-body body)  {:status 400}
            (if (already-exists? db email)
              {:status 409}
              (let [id (:id (usql/create-user db (update body :password utils/hash-password)))]
                {:status 200
                :body
                {:result
                 (select-keys (usql/get-user db :id id) [:id :name :created_at :email])}})))))

;; (s/def ::get-user-info-path-params (s/keys :req-un [::users/id ::tokens/token]))
(defn get-user-info-handler
  "get user info handler
  returns:
  - 400 invalid args
  - 403 forbidden
  "
  [{:keys [parameters headers path-params db]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        id (-> path-params :id Integer/parseInt)]
    (if-not (and (s/valid? ::tokens/token authorization) (s/valid? ::users/id id ))
      {:status 400}
      (if (-> (token/check-token-exists? db id authorization) count zero?)
        {:status 403}
        {:status 200
         :body
         {:result
          (select-keys (usql/get-user db :id id) [:name :created_at :email])}}))))

(s/def ::update-user-info (s/keys :req-un [::users/name ::users/email ::users/password]))
(defn update-user-info-handler
  "update user info handler
  returns:
  - 400 bad request
  - 403 forbidden
  - 404 not found
  -  409 conflict
  "
  [{:keys [parameters headers path-params db]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        id (-> path-params :id Integer/parseInt)
        {{:keys [name email password] :as body} :body} parameters]
    (if-not
        (and (s/valid? ::tokens/token authorization) (s/valid? ::users/id id)
             (s/valid? ::update-user-info body))
      {:status 400}
      (if-not (token/check-token-exists? db id authorization)
        {:status 403}
        (let [candidate (already-exists? db email)]
          (if (and candidate (not= (:id candidate) id))
              {:status 409}
            (if (zero? (usql/update-user  db (update body :password utils/hash-password) {:id id}))
              {:status 404}
              {:status 200
               :body {:result
                      (select-keys (usql/get-user db :id id) [:name :created_at :updated_at :email])}})))))))

(defn delete-user-handler
  "delete user handler
  returns
  - 400 bad request
  - 403 forbidden
  - 401 unauthorized
  - 404 not found
  "
  [{:keys [parameters headers path-params db]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        id (-> path-params :id Integer/parseInt)
        {{:keys [password]} :query} parameters]
    (if-not
        (and (s/valid? ::users/password password)
             (s/valid? ::users/id id) (s/valid? ::tokens/token authorization))
      {:status 400}
      (if-not (token/check-token-exists? db id authorization)
        {:status 403}
        (if-not (utils/check-password (usql/get-user db :id id) password)
          {:status 401}
          (if (zero? (usql/delete-user db {:id id}))
            {:status 404}
            (do
              (utsql/delete-all-token db id)
             {:status 200})))))))

(defn logout-handler
  "logout handler
  returns:
  - 400 bad request
  - 404 not found (token is not stored)"
  [{:keys [path-params headers db]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        id (-> path-params :id Integer/parseInt)]
    (if-not
        (and (s/valid? ::users/id id) (s/valid? ::tokens/token authorization))
      {:status 400}
      (if (zero? (utsql/delete-token db id authorization))
        {:status 404}
        {:status 200}))))

(defn logout-all-handler
  "logout all handler
  returns:
  - 400 bad request
  - 403 forbidden
  - 404 not found"
  [{:keys [path-params headers db]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        id (-> path-params :id Integer/parseInt)]
    (if-not (and (s/valid? ::users/id id) (s/valid?  ::tokens/token authorization))
      {:status 400}
      (if-not (token/check-token-exists? db id authorization)
        {:status 403}
        (if (zero? (utsql/delete-all-token db id))
          {:status 404}
          {:status 200})))))

;;; debug functions ;;;
;; ref https://github.com/seancorfield/next-jdbc/blob/master/doc/friendly-sql-functions.dm
;; find-by-keys or get-by-keys
;; (defn get-user
;;   "k is :id or :email
;;   v is value
;;   - returns
;;   map or nil (not found)"
;;   [db k v]
;;   {:id 1
;;    :name "meguru"
;;    :password "bcrypt+sha512$a6e0ed404a294d2779da5d133029c540$12$2213644dd237d2f722142898a7346da1724ebf996ef503f2"
;;    :email "debug@d.gmail.com"
;;    :created_at "inst of sql"
;;    :updated_at "inst of sql"
;;    :is_deleted false})

;; (defn create-user
;;   "m has key :id and :email and :name and :password (hashed)"
;;   [db m]
;;   (assoc m))

;; (defn update-user
;;   "m has any key
;;   idm is {:id id}"
;;   [db m idm]
;;   1)

;; (defn delete-user
;;   "idm is {:id id}"
;;   [db idm]
;;   1)

