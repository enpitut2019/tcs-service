(ns task-cabinet-server.service.user-token)

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
  ([]
   (random-token 127))
  ([length]
   (apply str (take length (repeatedly random-char)))))
