(ns task-cabinet-server.spec.user-token
  (:require [clojure.spec.alpha :as s]
            [task-cabinet-server.spec.utils :as utils]))

(s/def ::id pos-int?)

(s/def ::user-id pos-int?)

(s/def ::token
  (s/and string?
         #(= 127 (count %))
         #(re-matches #"^[a-zA-Z0-9]*$" %)))

(s/def ::created_at pos-int?)
