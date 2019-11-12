(ns task-cabinet-server.service.users
  (:require
   [task-cabinet-server.service.user-token :as token]
   [task-cabinet-server.spec.users :as users]
   [task-cabinet-server.spec.user-token :as tokens]
   [task-cabinet-server.service.utils :as utils]
   [clojure.spec.alpha :as s]
   [clojure.walk :as w]))


;;; debug functions ;;;
;; ref https://github.com/seancorfield/next-jdbc/blob/master/doc/friendly-sql-functions.dm
;; find-by-keys
(defn get-user
  "m has key :id or :email"
  [m]
  {:id 1
   :name "meguru"
   :password "bcrypt+sha512$a6e0ed404a294d2779da5d133029c540$12$2213644dd237d2f722142898a7346da1724ebf996ef503f2"
   :email "debug@d.gmail.com"
   :created_at "inst of sql"
   :updated_at "inst of sql"
   :is_deleted false})

(defn create-user
  "m has key :id and :email and :name"
  [m]
  (assoc m :id 2))

(defn update-user [m]
  1)

(defn delete-user [m]
  1)


;;; utils ;;;
(defn already-exists?
  "check user is exists in database
  args:
  - email: email address
  returns:
  user or nil"
  [email]
  (-> {:email email} get-user)
  false)

(defn conform-user
  "check user's information is valid
  args:
  - email: email address
  - password: un-hashed-password
  returns:
  user or nil"
  [email password]
  (-> {:email email} get-user
      (utils/check-password password)))

;;; handler & spec ;;;
(s/def ::user-login-body (s/keys :req-un [::users/email ::users/password]))
(defn login-handler
  "login handler
  returns:
  - 400 invalid args
  - 401 unauthorized"
  [{{{:keys [email password] :as body} :body} :parameters}]
  (if-not (s/valid? ::user-login-body body) {:status 400}
          (if-let [user-info (conform-user email password)]
            {:status 200
             :body
             {:result
              {:id (-> user-info :id)
               :token (token/garanteed-random-token (:id user-info))}}}
            {:status 401})))

(s/def ::user-create-body (s/keys :req-un [::users/name ::users/email ::users/password]))
(defn create-user-handler
  "create user handler
  returns:
  - 400 invalid args
  - 401 unauthorized"
  [{{{:keys [name password email] :as body} :body} :parameters}]
  (if-not (s/valid? ::user-create-body body)  {:status 400}
          (if (already-exists? email)
            {:status 401}
            (let [id (:id (create-user (update body :password utils/hash-password)))]
              {:status 200
               :body
               {:result
                (select-keys (get-user {:id id}) [:name :created_at :email])}}))))

;; (s/def ::get-user-info-path-params (s/keys :req-un [::users/id ::tokens/token]))
(defn get-user-info-handler
  "get user info handler
  returns:
  - 400 invalid args
  - 403 forbidden
  "
  [{:keys [parameters headers path-params]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        id (-> path-params :id Integer/parseInt)]
    (if-not (and (s/valid? ::tokens/token authorization) (s/valid? ::users/id id ))
      {:status 400}
      (if-not (token/check-token-exists? id authorization)
        {:status 403}
        {:status 200
         :body
         {:result
          (select-keys (get-user {:id id}) [:name :created_at :email])}}))))

(s/def ::update-user-info (s/keys :req-un [::users/id ::users/email ::users/password]))
(defn update-user-info-handler
  "update user info handler
  returns:
  - 400 bad request
  - 403 forbidden
  - 404 not found
  "
  [{:keys [parameters headers path-pararams]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        id (-> path-pararams :id Integer/parseInt)
        {{:keys [name email password] :as body} :body} parameters]
    (if-not
        (and (s/valid? ::tokens/token authorization) (s/valid? ::users/id id)
             (s/valid? ::update-user-info body))
      {:status 400}
      (if-not (token/check-token-exists? id authorization)
        {:status 403}
        (if-not (zero? (update-user  (assoc (update body :password utils/hash-password) :id id)))
          {:status 404}
          {:status 200
           :body {:result
                  (select-keys (get-user {:id id}) [:name :created_at :updated_at :email])}})))))

(defn delete-user-handler
  "delete user handler
  returns
  - 400 bad request
  - 403 forbidden
  - 401 unauthorized
  - 404 not found
  "
  [{:keys [parameters headers path-params]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        id (-> path-params :id Integer/parseInt)
        {{:keys [password]} :query} parameters]
    (if-not
        (and (s/valid? ::users/password password)
             (s/valid? ::users/id id) (s/valid? ::tokens/token authorization))
      {:status 400}
      (if-not (token/check-token-exists? id authorization)
        {:status 403}
        (if-not (utils/check-password (get-user {:id id}) password)
          {:status 401}
          (if (zero? (delete-user {:id id}))
            {:status 404}
            {:status 200}))))))

(defn logout-handler
  "logout handler
  returns:
  - 400 bad request
  - 404 not found (token is not stored)"
  [{:keys [path-params headers]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        id (-> path-params :id Integer/parseInt)]
    (if-not
        (and (s/valid? ::users/id id) (s/valid? ::tokens/token authorization))
      {:status 400}
      (if (zero? (token/delete-token id authorization))
        {:status 404}
        {:status 200}))))

(defn logout-all-handler
  "logout all handler
  returns:
  - 400 bad request
  - 403 forbidden
  - 404 not found"
  [{:keys [path-params headers]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        id (-> path-params :id Integer/parseInt)]
    (if (and (s/valid? ::users/id id) (s/valid?  ::tokens/token authorization))
      {:status 400}
      (if-not (token/check-token-exists? id authorization)
        {:status 403}
       (if (zero? (token/delete-all-token id))
          {:status 404}
          {:status 200})))))
