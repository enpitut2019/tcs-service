(ns task-cabinet-server.service.user-alg
  (:require
   [task-cabinet-server.spec.users :as users]
   [task-cabinet-server.spec.user-alg :as algs]
   [task-cabinet-server.spec.user-token :as tokens]
   [task-cabinet-server.service.user-token :as token]
   [task-cabinet-server.Boundary.select-alg :as select-algb]
   [clojure.spec.alpha :as s]
   [clojure.walk :as w]))

(defn add-user-alg!
  "add a selected algorithm for the user
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
    (println "add " authorization " " user-id " " type)
    (cond
      (not (s/valid? ::algs/type type))
      {:status 400 :body (s/explain-data ::algs/type type)}
      (not (s/valid? ::users/id user-id))
      {:status 400 :body (s/explain-data ::users/id user-id)}
      (not (s/valid? ::tokens/token authorization))
      {:status 400 :body (s/explain-data ::tokens/token authorization)}
      (-> (token/check-token-exists? db user-id authorization) count zero?)
      {:status 403}
      :default
      (if (pos-int? (select-algb/update-counter! db user-id type))
        {:status 200
         :body  {:result true}}
        {:status 500
         :body {:result "unexpected error"}}))))

(defn get-user-alg
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
      (let [base (select-algb/get-counter db user-id)
            res (if (-> base count zero?)
                  -1
                  (:alg  (apply max-key :value base)))]
        {:status 200
         :body {:type res}}))))

(defn get-user-alg-stats
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
      (let [res (->>
                 (select-algb/get-counter db user-id)
                 (map (fn [m] {:type (:alg m) :value (:value m)})))]
        {:status 200
         :body {:stats res}}))))
