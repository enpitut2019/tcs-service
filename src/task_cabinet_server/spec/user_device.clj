(ns task-cabinet-server.spec.user-device
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [task-cabinet-server.spec.utils :as utils]))

(s/def ::id pos-int?)

(s/def ::user_id pos-int?)

(s/def ::created_at pos-int?)

(s/def ::endpoint
  (s/and string?
         #(filter (partial string/includes? %)
                  ["android.googleapis.com"
                   "fcm.googleapis.com"
                   "updates.push.services.mozilla.com"
                   "updates-autopush.stage.mozaws.net"
                   "updates-autopush.dev.mozaws.net"
                   ".notify.windows.com"])))

(s/def ::auth
  (s/and string?
         utils/check-trim
         #(< 10 (count %) 255)))

(s/def ::p256dh
  (s/and string?
         utils/check-trim
         #(< 20 (count %) 255)))
