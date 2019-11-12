(ns task-cabinet-server.service.user-token)

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

(defn check-token-exists? [id token]
  "TODO Implement"
  false)

(defn garanteed-random-token
  ([id]
   (garanteed-random-token id 127))
  ([id length]
   (let [token (random-token length)]
     (if-not (check-token-exists? id token)
       token
       (garanteed-random-token id)))))

(defn delete-token
  [id token]
  ;; ok
  1)

(defn delete-all-token
  [id]
  1)
