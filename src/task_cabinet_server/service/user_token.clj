(ns task-cabinet-server.service.user-token
  (:require
   [task-cabinet-server.Boundary.user-token :as utsql]))

;;; utils ;;;
(def num-big-little-char
  (map char (concat
              (range 97 123)
              (range 65 91)
              (range 48 58))))

(def num-big-little-char-len
  (count num-big-little-char))

(defn random-char []
  (nth num-big-little-char (rand num-big-little-char-len)))

(defn random-token
  [length]
  (apply str (take length (repeatedly random-char))))

(defn add-token [db id token]
  (utsql/add-token db id token)
  token)

(defn check-token-exists? [db id token]
  (utsql/get-token db id token))

(defn garanteed-random-token
  ([id db]
   (garanteed-random-token id 127 db))
  ([id length db]
   (let [token (random-token length)]
     (if (zero? (count (check-token-exists? db id token)))
       (add-token db id token)
       (garanteed-random-token id length db)))))

;; (defn delete-token
;;   [db id token]
;;   ;; ok
;;   1)

;; (defn delete-all-token
;;   [db id]
;;   1)
