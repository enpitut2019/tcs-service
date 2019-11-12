(ns task-cabinet-server.handler.users
  (:require [clojure.spec.alpha :as s]
            [clojure.walk :as w]
            [clj-time.coerce :as c]
            [task-cabinet-server.service.users :as users]))

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
   :parameters {:body ::user-login-param}
   :responses {200 {:body {:result
                           {:token string? :id int?}}}}
   :handler users/login-handler
   })

(def create-user
  {:summary "create a user"
   :parameters {:body ::user-create-param}
   :responses {201 {:body {:result ::user-get-response}}}
   :handler users/create-user-handler})

(def get-user-info
  {:summary "get user information"
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters {:path ::path-params}
   :responses {200 {:body {:result ::user-get-response}}}
   :handler users/get-user-info-handler})

(def update-user-info
  {:summary "update user information"
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters {:path ::path-params
                :body ::users-update-param}
   :responses {200 {:body {:result ::user-update-response}}}
   :handler users/update-user-info-handler})

(def delete-user
  {:summary "delete user"
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters {:path ::path-params
                :query {:password ::password}}
   :handler users/delete-user-handler})

(def logout
  {:summary "logout"
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters {:path ::path-params}
   :responses {200 nil}
   :handler users/logout-handler})

(def logout-all
  {:summary "logout"
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters {:path ::path-params}
   :responses {200 nil}
   :handler users/logout-all-handler})

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

