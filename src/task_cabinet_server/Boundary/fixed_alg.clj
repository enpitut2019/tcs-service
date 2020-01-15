(ns task-cabinet-server.Boundary.fixed-alg
  (:require
   [next.jdbc :as jdbc]
   [task-cabinet-server.Boundary.utils.util :as util]))

(defprotocol Fixed-Alg
  (update-alg! [db user-id alg])
  (get-alg [db user-id]))

(extend-protocol Fixed-Alg
  task_cabinet_server.Boundary.utils.sql.Boundary
    (update-alg! [{:keys [spec]} user-id alg]
    (let [m {:user_id user-id
             :alg alg}
          confks [:user_id]
          update_funcstr (str "alg=" alg)]
      (util/upsert! spec :fixed_alg m confks update_funcstr)))
  (get-alg [{:keys [spec]} user-id]
    (util/find-by-m spec :fixed_alg {:user_id user-id})))


;; (defonce inst (task-cabinet-server.Boundary.utils.sql/->Boundary {:datasource (hikari-cp.core/make-datasource {:jdbc-url (environ.core/env :database-url)})}))

;; (get-alg inst 1)
;; (update-alg! inst 1 3)
;; (get-alg inst 1)
;; (update-alg! inst 1 3)
;; (get-alg inst 1)
;; (update-alg! inst 1 2)
;; (get-alg inst 1)
