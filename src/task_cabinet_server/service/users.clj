(ns task-cabinet-server.service.users
  (:require
   [task-cabinet-server.service.user-token :as token]
   [clojure.spec.alpha :as s]))


(defn get-user
  "m has key :id or :email"
  [m]
  {:id 1
   :name "meguru"
   :password "bcrypt+..."
   :email "hoge@gmail.com"
   :created_at "inst of sql"
   :updated_at "inst of sql"
   :is_deleted false})



(defn login [{{{:keys [email password]} :body} :parameters}]
  (let [ user-info (-> {:email email} get-user (check-password password))]
    ))
