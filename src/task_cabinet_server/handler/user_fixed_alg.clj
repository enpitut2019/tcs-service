(ns task-cabinet-server.handler.user-fixed-alg
  (:require
   [task-cabinet-server.Boundary.fixed-alg :as falgb]
   [task-cabinet-server.spec.user-fixed-alg :as falgs]
   [task-cabinet-server.service.user-fixed-alg :as falg]
   [clojure.spec.alpha :as s]
   [clojure.walk :as w]))

(s/def ::user-id  pos-int?)
(s/def ::endpoint string?)
(s/def ::auth string?)
(s/def ::p256dh string?)

(s/def ::path-params (s/keys :req-un [::user-id]))
(s/def ::add-user-fixed-alg-params (s/keys :req-un [::falgs/type]))

(def add-user-fixed-alg!
  {:summary "add user fixed algorithm for the user"
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters
   {:path ::path-params
    :body ::add-user-fixed-alg-params}
   :responses {200 {:body {:result boolean?}}}
   :handler
   falg/add-user-fixed-alg!
   ;; (fn [{:keys [parameters headers path-params]}]
   ;;   (let [{:keys [authorization]} (w/keywordize-keys headers)
   ;;         id (-> path-params :user-id Integer/parseInt)
   ;;         {{:keys [type]} :body} parameters
   ;;         ]
   ;;     (cond
   ;;       (not
   ;;        (and
   ;;         (s/valid? ::falgs/type type)
   ;;         (s/valid? ::auth authorization)))
   ;;       {:status 403}
   ;;       :default
   ;;       {:status 200
   ;;        :body {:result true}})))
   })

(def get-user-fixed-alg
  {:summary "get user fixed algorithm for the user"
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters
   {:path ::path-params}
   :responses {200 {:body {:type integer?}}}
   :handler
   falg/get-user-fixed-alg
   ;; (fn [{:keys [parameters headers path-params]}]
   ;;   {:status 200 :body {:type 1}})
   })

(defn user-fixed-alg-app [env]
  ["/tcs/users/:user-id"
   {:swagger {:tags ["fixed-algorithm"]}}
   ["/fixed-algorithm"
    {:post add-user-fixed-alg!
     :get get-user-fixed-alg}]])
