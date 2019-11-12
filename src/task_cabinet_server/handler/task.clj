(ns task-cabinet-server.handler.task
  (:require [clojure.spec.alpha :as s]
            [clojure.walk :as w]
            [clj-time.coerce :as c]
            ))

(s/def ::user-id int?)
(s/def ::id int?)
(s/def ::path-params (s/keys :req-un [::user-id]))
(s/def ::task-path-params (s/keys :req-un [::user-id ::id]))

(s/def ::name string?)
(s/def ::description string?)
(s/def ::category string?)
(s/def ::deadline int?)
(s/def ::estimate int?)
(s/def ::created_at int?)
(s/def ::updated_at int?)
(s/def ::finished_at int?)
(s/def ::all boolean?)
(s/def ::abst-task
  (s/keys :req-un [::id ::name ::deadline ::estimate ::created_at]
          :opt-un [::finished_at ::category]))
(s/def ::create-task-params
  (s/keys :req-un [::name ::deadline ::estimate]
          :opt-un [::description ::category]))
(s/def ::create-task-response
  (s/keys :req-un [::id ::name ::deadline ::estimate ::created_at]
          :opt-un [::description ::category]))
(s/def ::get-task-response
  (s/keys :req-un [::id ::name ::deadline ::estimate ::created_at ::updated_at]
          :opt-un [::description ::category ::finished_at ::updated_at]))
(s/def ::task-update-params
  (s/keys :req-un [::name ::deadline ::estimate]
          :opt-un [::description ::category]))
(s/def ::task-update-response
  (s/keys ::req-un [::name ::deadline ::estimate ::created_at ::updated_at]
          :opt-un [::description ::category  ::finished_at]))
(s/def ::get-task-list-response
  (s/* ::abst-task))

(def tmp-task-list
  [{:id 1
    :name "Implement Server"
    :deadline 1573493099290
    :estimate 40
    :created_at 1572566411400}
   {:id 2
    :name "Implement WebPush"
    :deadline 1572567452000
    :estimate 80
    :created_at 1572566431400
    :finished_at 1572567432000
    :category "server"}
   {:id 3
    :name "Implement Authorization"
    :deadline 1572566451400
    :estimate 60
    :created_at 1572566441400
    :category "server"}
   {:id 4
    :name "Re: check database structure"
    :deadline 1572566952000
    :estimate 100
    :created_at 1572566411500
    :finished_at 1572567431000}])
;; (s/explain ::get-task-list-response tmp-task-list)


(def create-task
  {:summary "create a task"
   :description "for debug id \"1 \"  authorization  \"gXqi4mnXg8KyuSKS5XlK\""
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters
   {:path ::path-params
    :body ::create-task-params}
   :responses {201 {:body {:result ::create-task-response}}}
   :handler
   (fn [{:keys [path-params headers parameters]}]
     (let [{:keys [authorization]} (w/keywordize-keys headers)
           user-id (-> path-params :user-id Integer/parseInt)
           {{:keys [name deadline estimate description category]} :body} parameters]
       (if (and (= user-id 1) (= authorization "gXqi4mnXg8KyuSKS5XlK"))
        (let [res {:name name
                   :deadline deadline
                   :estimate estimate}
              res (if description (assoc res :description description) res)
              res (if category (assoc res :category category) res)
              res (assoc res :created_at 1572566412000)
              res (assoc res :id 2)]
          {:status 201
           :body {:result res}})
        {:status 401})))})

(def get-task-info
  {:summary  "get a task-info"
   :description "for debug user-id and id \"1 \"  authorization  \"gXqi4mnXg8KyuSKS5XlK\""
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters
   {:path ::task-path-params}
   :responses {200 {:body {:result ::get-task-response}}}
   :handler
   (fn [{:keys [path-params headers]}]
     (let [{:keys [authorization]} (w/keywordize-keys headers)
           user-id (-> path-params :user-id Integer/parseInt)
           id (-> path-params :id Integer/parseInt)]
       (if (and (= id 1) (= user-id 1) (= authorization "gXqi4mnXg8KyuSKS5XlK"))
        {:status 201
          :body
         {:result
          (nth tmp-task-list 0)}}
        {:status 401})))})

(def delete-task
  {:summary  "delete a task-info"
   :description "for debug user-id and id \"1 \"  authorization  \"gXqi4mnXg8KyuSKS5XlK\""
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters
   {:path ::task-path-params}
   :responses {200 nil}
   :handler
   (fn [{:keys [path-params headers]}]
     (let [{:keys [authorization]} (w/keywordize-keys headers)
           user-id (-> path-params :user-id Integer/parseInt)
           id (-> path-params :id Integer/parseInt)]
       (if (and (= user-id 1) (= authorization "gXqi4mnXg8KyuSKS5XlK"))
         (if  (= id 1) {:status 200}
              {:status 404})
         {:status 401})))})

(def update-task
  {:summary "update a task information"
   :description "for debug user and task id  \"1\" authorization \"gXqi4mnXg8KyuSKS5XlK\""
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters
   {:path ::task-path-params
    :body ::task-update-params}
   :responses {200 {:body {:result ::task-update-response}}}
   :handler
   (fn [{:keys [path-params headers parameters]}]
     (let [{:keys [authorization]} (w/keywordize-keys headers)
           user-id (-> path-params :user-id Integer/parseInt)
           id (-> path-params :id Integer/parseInt)
           {{:keys [name deadline estimate description category]} :body} parameters]
       (if (and (= user-id 1) (= authorization "gXqi4mnXg8KyuSKS5XlK"))
         (if (= id 1)
           (let [res {:name name :deadline deadline :estimate estimate}
                 res (if description (assoc res :description description) res)
                 res (if category (assoc res :category category) res)
                 res (assoc res :created_at 1572566412000)
                 res (assoc res :updated_at (-> (clj-time.core/now) c/to-long))]
             {:stastus 200
              :body {:result res}})
           {:status 404})
         {:status 401})))})

(def complete-task
  {:summary "complete a task"
   :description "for debug user and task id  \"1\" authorization \"gXqi4mnXg8KyuSKS5XlK\""
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters {:path ::task-path-params}
   :responses {200 nil}
   :handler (fn [{:keys [path-params headers]}]
              (let [{:keys [authorization]} (w/keywordize-keys headers)
                    id (-> path-params :id Integer/parseInt)
                    user-id (-> path-params :user-id Integer/parseInt)]
                (if (and (= authorization "gXqi4mnXg8KyuSKS5XlK") (= id 1))
                  (if (= id 1)
                    {:status 200}
                    {:status 404})
                  {:status 401})))})

(def get-list-task
  {:summary "get a task"
   :description "for debug id and user-id \"1\" authorization \"gXqi4mnXg8KyuSKS5XlK\""
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters {:path ::path-params
                :query {:all ::all}}
   :responses {200 {:body {:result ::get-task-list-response}}}
   :handler
   (fn [{:keys [parameters headers path-params]}]
     (let [{:keys [authorization]} (w/keywordize-keys headers)
           user-id (-> path-params :user-id Integer/parseInt)
           {{:keys [all]} :body} parameters]
       (if (and (= user-id 1) (= authorization "gXqi4mnXg8KyuSKS5XlK"))
         (if all
          {:status 200
            :body
            {:result
             tmp-task-list}}
          {:status 200
           :body
           {:result
            (doall (filter #(get  % :finished_at) tmp-task-list))}})
         {:status 401})))})

(defn task-app [env]
  ["/tcs"
   {:swagger {:tags ["task"]}}
   ["/user/:user-id/task"
    {:post create-task}]
   ["/user/:user-id/task/:id"
    {:get get-task-info
     :delete delete-task
     :patch update-task}]
   ["/user/:user-id/task/:id/complete"
    {:post complete-task}]
   ["/user/:user-id/task-list"
    {:get get-list-task}]])





;; (def dataset [6 12 18 30 42 48])
;; ;;; 6 48
;; (def first-clust [[6 12 18 24] [30 42 48]])
;; (def first-clust-mean (map #(/ (apply + %) (count %)) first-clust)) ;; 15 40
;; (def second-clust [[6 12 18 24] [30 42 48]])
;; (def second-clust-mean (map #(/ (apply + %) (count %)) second-clust)) ;; 15 40
;; ;; end

;; ;; 42 48
;; (def first-clust [[6 12 18 30 42] [48]])
;; (def first-clust-mean (map #(/ (apply + %) (count %)) first-clust)) ;; 108/5 48
;; (map #(- dataset %) first-clust-mean)

;; (- [1 2 3 4] 1)
