(ns task-cabinet-server.Boundary.select-alg
  (:require
   [next.jdbc :as jdbc]
   [task-cabinet-server.Boundary.utils.util :as util]))

(defprotocol Select-Alg
  (update-counter! [db user-id alg])
  (get-counter [db user-id]))

(extend-protocol Select-Alg
  task_cabinet_server.Boundary.utils.sql.Boundary
  (update-counter! [{:keys [spec]} user-id alg]
    (let [m {:user_id user-id
             :alg alg
             :value 1}
          confks [:user_id :alg]
          update_funcstr "value = select_alg.value + 1"]
      (util/upsert! spec :select_alg m confks update_funcstr)))
  (get-counter [{:keys [spec]} user-id]
    (util/find-by-m spec :select_alg {:user_id user-id})))

;; (update-counter! inst  1 1)
;; (get-counter inst 1)
;; (defonce inst (task-cabinet-server.Boundary.utils.sql/->Boundary {:datasource (hikari-cp.core/make-datasource {:jdbc-url (environ.core/env :database-url)})}))

