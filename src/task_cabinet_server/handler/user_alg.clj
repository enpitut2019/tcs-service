(ns task-cabinet-server.handler.user-alg
  (:require [clojure.spec.alpha :as s]
            [task-cabinet-server.spec.user-alg :as algs]
            [task-cabinet-server.spec.users :as users]
            [task-cabinet-server.service.user-alg :as salg]
            [clojure.walk :as w]))

(s/def ::user-id  pos-int?)
(s/def ::endpoint string?)
(s/def ::auth string?)
(s/def ::p256dh string?)

(s/def ::path-params (s/keys :req-un [::user-id]))
(s/def ::add-user-alg-params (s/keys :req-un [::algs/type]))

(def add-user-alg
  {:summary "add a selected algorithm for the user"
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters
   {:path ::path-params
    :body ::add-user-alg-params}
   :responses {200 {:body {:result boolean?}}}
   :handler
   salg/add-user-alg!
   ;; (fn [{:keys [parameters headers path-params]}]
   ;;   (let [{:keys [authorization]} (w/keywordize-keys headers)
   ;;         id (-> path-params :user-id Integer/parseInt)
   ;;         {{:keys [type]} :body} parameters]
   ;;     (if (and
   ;;          (s/valid? ::algs/type type)
   ;;          (s/valid? ::users/id  id))
   ;;       {:status 200
   ;;        :body
   ;;        {:result true}}
   ;;       {:status 403
   ;;        :body
   ;;        [(s/explain ::path-params path-params)
   ;;         (s/explain ::algs/type type)]})))
   })

(def get-user-alg
  {:summary "get selected algorithms for the user"
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters
   {:path ::path-params}
   :responses {200 {:body {:type integer?}}}
   :handler
   salg/get-user-alg
   ;; (fn [{:keys [headers path-params]}]
   ;;   (let [{:keys [authorization]} (w/keywordize-keys headers)
   ;;         id (-> path-params :user-id Integer/parseInt)]
   ;;     (if (s/valid? ::users/id  id)
   ;;       {:status 200
   ;;        :body
   ;;        {:type 1}}
   ;;       {:status 403
   ;;        :body
   ;;        [(s/explain ::path-params path-params)]})))
   })

(def get-user-alg-stats
  {:summary "get selected algorithms for the user"
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters
   {:path ::path-params}
   :handler
   salg/get-user-alg-stats})

(defn user-alg-app [env]
  ["/tcs/users/:user-id"
   {:swagger {:tags ["algorithm"]}}
   ["/algorithm/stats"
    {:get get-user-alg-stats}]
   ["/algorithm"
    {:post add-user-alg
     :get get-user-alg}
    ]])
