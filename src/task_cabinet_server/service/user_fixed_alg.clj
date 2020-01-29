(ns task-cabinet-server.service.user-fixed-alg
  (:require
   [task-cabinet-server.spec.users :as users]
   [task-cabinet-server.spec.user-fixed-alg :as falgs]
   [task-cabinet-server.spec.user-token :as tokens]
   [task-cabinet-server.service.user-token :as token]
   [task-cabinet-server.Boundary.fixed-alg :as fixed-algb]
   [clojure.spec.alpha :as s]
   [clojure.walk :as w]))


(defn add-user-fixed-alg!
  "add or update a selected algorithm for the user
  args:
  - map
    - parameters
    - headers
    - path-params
  "
  [{:keys [parameters headers path-params db]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        user-id (-> path-params :user-id Integer/parseInt)
        {{:keys [type]} :body} parameters]
    (cond
      (not (s/valid? ::falgs/type type))
      {:status 400 :body (s/explain-data ::falgs/type type)}
      (not (s/valid? ::users/id user-id))
      {:status 400 :body (s/explain-data ::users/id user-id)}
      (not (s/valid? ::tokens/token authorization))
      {:status 400 :body (s/explain-data ::tokens/token authorization)}
      (-> (token/check-token-exists? db user-id authorization) count zero?)
      {:status 403}
      :default
      (if (pos-int? (fixed-algb/update-alg! db user-id type))
        {:status 200
         :body {:result true}}
        {:status 500
         :body {:result "unexpected error"}}))))

(defn get-user-fixed-alg
  ""
  [{:keys [headers path-params db]}]
  (let [{:keys [authorization]} (w/keywordize-keys headers)
        user-id (-> path-params :user-id Integer/parseInt)]
    (cond
      (not (s/valid? ::users/id user-id))
      {:status 400 :body (s/explain-data ::users/id user-id)}
      (not (s/valid? ::tokens/token authorization))
      {:status 400 :body (s/explain-data ::tokens/token authorization)}
      (-> (token/check-token-exists? db user-id authorization) count zero?)
      {:status 403}
      :default
      (let [base (fixed-algb/get-alg db user-id)
            res (if (-> base count zero?)
                  -1
                  (:alg (first base)))]
        {:status 200
         :body {:type res}}))))
