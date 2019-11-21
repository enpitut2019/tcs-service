(ns task-cabinet-server.handler.utils.util
  (:require [ring.middleware.cors :refer [wrap-cors]]))

;; https://enpitut2019.github.io/
;; http://localhost
;; https://localhost

(def default-origin
  [#"http://localhost:4200" #"https://localhost:4200" #"http://localhost" #"https://localhost" #"https://enpitut2019.github.io"])

(defn cors-handler [handler]
  (wrap-cors handler :access-control-allow-origin default-origin))
