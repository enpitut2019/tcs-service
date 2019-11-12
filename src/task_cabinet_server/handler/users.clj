(ns task-cabinet-server.handler.users
  (:require [clojure.spec.alpha :as s]
            [clojure.walk :as w]
            [clj-time.coerce :as c]))

(s/def ::id int?)
(s/def ::name string?)
(s/def ::email string?)
(s/def ::password string?)
(s/def ::updated_at int?)
(s/def ::created_at int?)
(s/def ::path-params (s/keys :req-un [::id]))
(s/def ::users-update-param (s/keys :req-un [::name ::email ::password]))
(s/def ::user-update-response (s/keys :req-un [::name ::created_at ::updated_at] :opt-un [::email]))
(s/def ::user-get-response (s/keys :req-un [::name ::created_at] :opt-un [::email]))
(s/def ::user-create-param (s/keys :req-un [::name ::password] :opt-un [::email]))
(s/def ::user-create-response (s/keys :req-un [::name ::created_at] :opt-un [::email]))
(s/def ::user-login-param (s/keys :req-un [::email ::password]))

(def login
  {:summary "login"
   :description "for debug email \"debug@d.gmail.com\"password \"testPass10\""
   :parameters {:body ::user-login-param}
   :responses {200 {:body {:result
                           {:token string? :id int?}
                           }}}
   :handler
   (fn [{{{:keys [email password]} :body} :parameters}]
     (if (and (= email "debug@d.gmail.com") (= password "testPass10"))
       {:status 200
        :body
        {:result
         {:token "gXqi4mnXg8KyuSKS5XlK"
          :id 1}}}
       {:status 404}))})

(def create-user
  {:summary "create a user"
   :description "for debug name \"Debug User-2\" password \"testPass11\""
   :parameters {:body ::user-create-param}
   :responses {201 {:body {:result ::user-get-response}}}
   :handler
   (fn [{{{:keys [name password email]}:body} :parameters}]
     (if (and (= name "Debug User-2") (= password "testPass11"))
       (if email
         {:status 201
          :body {:result
                 {:name name
                   :created_at 1572566400000
                   :email email}}}
         {:status 201
          :body {:result
                 {:name name
                   :created_at 1572566400000}}})
       {:status 500
        :body "The user already exists."}))})

(def get-user-info
  {:summary "get user information"
   :description "for debug id \"1\" authorization  \"gXqi4mnXg8KyuSKS5XlK\""
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters {:path ::path-params}
   :responses {200 {:body {:result ::user-get-response}}}
   :handler (fn [{:keys [parameters headers path-params]}]
              (let [{:keys [authorization]} (w/keywordize-keys headers)
                    id (-> path-params :id Integer/parseInt)]
                (if (and (= id 1) (= authorization "gXqi4mnXg8KyuSKS5XlK"))
                  {:status 200
                   :body
                   {:result
                    {:name "Debug User"
                     :created_at 1572566400000
                     :updated_at 1572566411000
                     :email "debug@d.gmail.com"}}}
                  {:status 401})))})

(def update-user-info
  {:summary "update user information"
   :description "for debug id \"1\" password \"testPass10\" authorization\"gXqi4mnXg8KyuSKS5XlK\""
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters {:path ::path-params
                :body ::users-update-param}
   :responses {200 {:body {:result ::user-update-response}}}
   :handler (fn [{:keys [parameters headers path-params]}]
              (let [{:keys [authorization]} (w/keywordize-keys headers)
                    id (-> path-params :id Integer/parseInt)
                    {{:keys [name email password]} :body} parameters]
                (if (and (= id 1) (= authorization "gXqi4mnXg8KyuSKS5XlK") (= password "testPass10"))
                  {:status 200
                   :body
                   {:result
                    {:name name
                     :created_at 1572566400000
                     :updated_at (-> (clj-time.core/now) c/to-long)
                     :email email}}}
                  {:status 401})))})

(def delete-user
  {:summary "delete user"
   :description "for debug password is  \"testPass10\""
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters {:path ::path-params
                :query {:password ::password}}
   :handler (fn [{:keys [parameters headers path-params]}]
              (let [{:keys [authorization]} (w/keywordize-keys headers)
                    id (-> path-params :id Integer/parseInt)
                    {{:keys [password]} :query} parameters]
                (if (and (= id 1) (= authorization "gXqi4mnXg8KyuSKS5XlK") (= password "testPass10"))
                  {:status 200}
                  {:status 401})))})

(def logout
  {:summary "logout"
   :description "in debug,  this endpoint does nothing"
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters {:path ::path-params}
   :responses {200 nil}
   :handler
   (fn [{:keys [path-params headers]}]
     (let [{:keys [authorization]} (w/keywordize-keys headers)
           id (-> path-params :id Integer/parseInt)]
       ;; TODO: add cond whether authorization is nil or not nil.
       {:status 200}))})

(def logout-all
  {:summary "logout"
   :description "in debug,  id \"1\" authorization \"gXqi4mnXg8KyuSKS5XlK\""
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters {:path ::path-params}
   :responses {200 nil}
   :handler
   (fn [{:keys [path-params headers]}]
     (let [{:keys [authorization]} (w/keywordize-keys headers)
           id (-> path-params :id Integer/parseInt)]
       (if (and (= id 1) (= authorization "gXqi4mnXg8KyuSKS5XlK"))
        ;; TODO: add cond whether authorization is nil or not nil.
         {:status 200}
         {:status 401})))})

(defn users-app [env]
  ["/tcs"
   {:swagger {:tags ["users"]}}
   ["/login"
    {:post login}]
   ["/user"
    {:post create-user}]
   ["/user/:id"
    {:get get-user-info
     :patch update-user-info
     :delete delete-user}]
   ["/user/:id/logout"
    {:put logout}]
   ["/user/:id/logout-all"
    {:put logout-all}]])

