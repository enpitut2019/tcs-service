(ns task-cabinet-server.spec.task
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clj-time.core :as time]
            [clj-time.coerce :as tc]
            [task-cabinet-server.spec.utils :as utils]))

;;; specs
(s/def ::id pos-int?)

(s/def ::user_id pos-int?)

(s/def ::name
  (s/and string?
         utils/check-trim
         #(<= 1 (count %) 63)))

(s/def ::description
  (s/and string?
         utils/check-trim
         #(<= 1 (count %) 511)))

(s/def ::category
  (s/and string?
         utils/check-trim
         #(<= 1 (count %) 255)))

(s/def ::deadline
  (s/and pos-int?
         #(>= % (-> (time/today) tc/to-long))))

(s/def ::estimate
  (s/and int?
         #(<= 0 % 100)))

(s/def ::finished_at pos-int?)
(s/def ::created_at pos-int?)
(s/def ::updated_at pos-int?)
(s/def ::is_deleted boolean?)

;; valid
;;(s/conform ::name "t")
;; (s/conform ::name "実☆装")
;; invalid
;; (s/conform ::name " ")
;; (s/conform ::name " hoge")
;; (s/conform ::name "")

;; valid
;; (s/conform ::description "hoge")
;; (s/conform ::description "生き残りたい")
;; invalid
;; (s/conform ::description "")

;; valid
;; (s/conform ::category "test")
;; invalid
;; (s/conform ::category "")

;; valid
;; (s/conform ::deadline (-> (time/plus (time/today) (time/days 1)) tc/to-long))
;; invalid
;; (s/conform ::deadline (-> (time/minus (time/today) (time/days 1)) tc/to-long))

;; valid
;; (s/conform ::estimate 100)
;; (s/conform ::estimate 0)
;; (s/conform ::estimate 1)
;; invalid
;; (s/conform ::estimate -1)
;; (s/conform ::estimate 0.1)
