(ns task-cabinet-server.handler.core
  (:require
   [reitit.ring :as ring]
   [reitit.coercion.spec]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [reitit.ring.coercion :as coercion]
   [reitit.dev.pretty :as pretty]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.ring.middleware.dev :as dev]
   [reitit.ring.spec :as spec]
   [spec-tools.spell :as spell]
   [ring.adapter.jetty :as jetty]
   [muuntaja.core :as m]
   [clojure.java.io :as io]

   [ring.logger :refer [wrap-with-logger]]
   [task-cabinet-server.handler.utils.util :as util]
   [integrant.core :as ig]
   [clojure.java.io :as io]
   [taoensso.timbre :as timbre]

   [task-cabinet-server.handler.users :refer [users-app]]
   [task-cabinet-server.handler.user-device :refer [user-device-app]]
   [task-cabinet-server.handler.task :refer [task-app]]))


(defn root-app [env]
  (ring/ring-handler
    (ring/router
      [["/swagger.json"
        {:get {:no-doc true
               :swagger {
                         :securityDefinitions {:ApiKeyAuth {:type "apiKey" :name "Authorization" :in "header"}}
                         :info {:title "task-cabinet-server REST API"
                                :version "0.0.1-dev"
                                :description "with reitit-ring, endpoint only"}}
               :handler (swagger/create-swagger-handler)}}]
       (users-app env)
       (user-device-app env)
       (task-app env)
       ["/files"
        {:swagger {:tags ["files"]}}
        ["/upload"
         {:post {:summary "upload a file"
                 :parameters {:multipart {:file multipart/temp-file-part}}
                 :responses {200 {:body {:name string?, :size int?}}}
                 :handler (fn [{{{:keys [file]} :multipart} :parameters}]
                            {:status 200
                             :body {:name (:filename file)
                                    :size (:size file)}})}}]
        ["/download"
         {:get {:summary "downloads a file"
                :swagger {:produces ["image/png"]}
                :handler (fn [_]
                           {:status 200
                            :headers {"Content-Type" "image/png"}
                            :body (-> "reitit.png"
                                      (io/resource)
                                      (io/input-stream))})}}]]

       ;; ["/math"
       ;;  {:swagger {:tags ["math"]}}
       ;;  ["/plus"
       ;;   {:get {:summary "plus with spec query parameters"
       ;;          :parameters {:query {:x int?, :y int?}}
       ;;          :responses {200 {:body {:total int?}}}
       ;;          :handler (fn [{{{:keys [x y]} :query} :parameters}]
       ;;                     {:status 200
       ;;                      :body {:total (+ x y)}})}
       ;;    :post {:summary "plus with spec body parameters"
       ;;           :parameters {:body {:x int?, :y int?}}
       ;;           :responses {200 {:body {:total int?}}}
       ;;           :handler (fn [{{{:keys [x y]} :body} :parameters}]
       ;;                      {:status 200
       ;;                       :body {:total (+ x y)}})}}]
       ;;  ["/minus"
       ;;   {:get {:summary "plus with spec query parameters"
       ;;          :parameters {:query {:x int?, :y int?}}
       ;;          :responses {200 {:body {:total int?}}}
       ;;          :handler (fn [{{{:keys [x y]} :query} :parameters}]
       ;;                     {:status 200
       ;;                      :body {:total (- x y)}})}
       ;;    :post {:summary "plus with spec body parameters"
       ;;           :parameters {:body {:x int?, :y int?}}
       ;;           :responses {200 {:body {:total int?}}}
       ;;           :handler (fn [{{{:keys [x y]} :body} :parameters}]
       ;;                      {:status 200
       ;;                       :body {:total (- x y)}})}}]]
       ]

      {;;:reitit.middleware/transform dev/print-request-diffs ;; pretty diffs
       ;;:validate spec/validate ;; enable spec validation for route data
       ;;:reitit.spec/wrap spell/closed ;; strict top-level validation
       :exception pretty/exception
       :data {:coercion reitit.coercion.spec/coercion
              :muuntaja m/instance
              :middleware [;; swagger feature
                           swagger/swagger-feature
                           ;; query-params & form-params
                           parameters/parameters-middleware
                           ;; content-negotiation
                           muuntaja/format-negotiate-middleware
                           ;; encoding response body
                           muuntaja/format-response-middleware
                           ;; exception handling
                           exception/exception-middleware
                           ;; decoding request body
                           muuntaja/format-request-middleware
                           ;; coercing response bodys
                           coercion/coerce-response-middleware
                           ;; coercing request parameters
                           coercion/coerce-request-middleware
                           ;; multipart
                           multipart/multipart-middleware]}})
    (ring/routes
      (swagger-ui/create-swagger-ui-handler
        {:path "/"
         :config {:validatorUrl nil
                  :operationsSorter "alpha"}})
      (ring/create-default-handler))
    {:middleware
     [util/cors-handler
      wrap-with-logger]}))


(defmethod ig/init-key ::handler [ _ {:keys [env]}]
  (root-app env))

(defmethod ig/init-key ::server [_ {:keys [env handler port]}]
  (timbre/info "Server is running in port " port)
  (jetty/run-jetty handler {:port port :join? false}))

(defmethod ig/halt-key! ::server [_ server]
  (.stop server))
