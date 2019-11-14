(ns task-cabinet-server.Boundary.utils.sql
  (:require
   [integrant.core :as ig]
   [taoensso.timbre :as timbre]
   [hikari-cp.core :as hikari-cp]))

(defrecord Boundary [spec])

(defmethod ig/init-key ::sql
  [_ {:keys [env] :as options}]
  (let [datasource
        (-> {:jdbc-url (:database-url env)}
            (hikari-cp/make-datasource))]
    (timbre/info "setup connection pool ...")
    (->Boundary {:datasource
                 datasource})))

(defmethod ig/halt-key! ::sql
  [_ boundary]
  (timbre/info "close connection pool ...")
  (-> boundary
      .spec
      :datasource
      hikari-cp/close-datasource))
