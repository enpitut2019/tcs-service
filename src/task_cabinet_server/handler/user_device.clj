(ns task-cabinet-server.handler.user-device
  (:require [clojure.spec.alpha :as s]
            [clojure.walk :as w]))

(s/def ::user-id int?)
(s/def ::endpoint string?)
(s/def ::auth string?)
(s/def ::p256dh string?)
(s/def ::password string?)
(s/def ::path-params (s/keys :req-un [::user-id]))
(s/def ::keys (s/keys :req-un [::auth ::p256dh]))
(s/def ::add-device-params (s/keys :req-un [::endpoint ::keys]))
(s/def ::add-device-response (s/keys :req-un [::endpoint ::keys]))
(s/def ::remove-device-params (s/keys :req-un [::endpoint ::keys]))
(s/def ::remove-all-device-params (s/keys :req-un [::password]))

(def add-device
  {:summary "add a device receives webpush"
   :description "for debug id \"1\" authorization  \"gXqi4mnXg8KyuSKS5XlK\""
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters
   {:path ::path-params
    :body ::add-device-params}
   :responses {200 {:body {:result ::add-device-response}}}
   :handler
   (fn [{:keys [parameters headers path-params]}]
     (let [{:keys [authorization]} (w/keywordize-keys headers)
           id (-> path-params :user-id Integer/parseInt)
           {{:keys [endpoint keys]} :body} parameters
           {:keys [auth p256dh]} keys]
       (if (and (= id 1) (= authorization "gXqi4mnXg8KyuSKS5XlK"))
         {:status 201
          :body {:result {:endpoint endpoint :keys keys}}}
         {:status 403})))})

(def remove-device
  {:summary "remove a device receives webpush"
   :description "for debug id \"1\" authorization \"gXqi4mnXg8KyuSKS5XlK\""
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters
   {:path ::path-params
    :body ::remove-device-params}
   :responses {200 nil}
   :handler
   (fn [{:keys [parameters headers path-params]}]
     (let [{:keys [authorization]} (w/keywordize-keys headers)
           id (-> path-params :user-id Integer/parseInt)
           {{:keys [endpoint keys]} :body} parameters
           {:keys [auth p256dh]} keys]
       (if (and (= id 1) (= authorization "gXqi4mnXg8KyuSKS5XlK"))
         {:status 200}
         {:status 403})))})

(def remove-all-device
  {:summary "remove a device receives webpush"
   :description "for debug id \"1\" authorization \"gXqi4mnXg8KyuSKS5XlK\" pass \"testPass10\""
   :swagger {:security [{:ApiKeyAuth []}]}
   :parameters
   {:path ::path-params
    :body ::remove-all-device-params}
   :responses {200 nil}
   :handler
   (fn [{:keys [parameters headers path-params]}]
     (let [{:keys [authorization]} (w/keywordize-keys headers)
           id (-> path-params :user-id Integer/parseInt)
           password (-> parameters :body :password)]
         (if (and (= id 1) (= authorization "gXqi4mnXg8KyuSKS5XlK") (= password "testPass10"))
        {:status 200}
        {:status 403})))})

(defn user-device-app [env]
  ["/tcs/users/:user-id"
   {:swagger {:tags ["device"]}}
   ["/device"
    {:post add-device
     :delete remove-device}]
   ["/device/remove-all"
    {:delete remove-all-device}]])
