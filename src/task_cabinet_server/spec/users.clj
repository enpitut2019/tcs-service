(ns task-cabinet-server.spec.users
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as string]
   [task-cabinet-server.spec.utils :as utils]))

;;;;;; regex ;;;;;;
(def user-name-regex
  ;; This SQL escape is excessive. because I use parametrized_SQL_statement
  ;; https://rosettacode.org/wiki/Parametrized_SQL_statement
  ;; this escape is because, To found a man who want to do SQL injection.
  ;; We will report the request which was failed this assertion.
  ;;
  ;; condition
  ;; >>> - be not contain & @ ^ ( ) [ ] { } . ? + * | \ ' "
  ;; #"[0-9a-zA-Zぁ-んァ-ヶ一-龠々ー]*$"
  #"^[^&@\^\(\)\[\]\{\}.\?\+\*\|\\\'\"]*$")

(def password-regex
  ;; condition
  ;; >>> - less 1 number
  ;; >>> - less 1 small alphabet
  ;; >>> - less 1 big alphabet
  #"^(?=.*\d+)(?=.*[a-z])(?=.*[A-Z])[A-Za-z\d+]*$")

(def email-regex
  #"^[\w-\.+]*[\w-\.]\@([\w]+\.)+[\w]+[\w]$")

;;; utils ;;;
(defn check-trim [s]
  (= (count s) (count (string/trim s))))

;;; specs
(s/def ::id pos-int?)

(s/def ::name
  (s/and string?
         #(re-matches user-name-regex %)
         utils/check-trim
         #(<= 2 (count %) 30)))

(s/def ::password
  (s/and string?
         #(re-matches password-regex %)
         utils/check-trim
         #(<= 5 (count %) 30)))

(s/def ::email
  (s/and string?
         #(re-matches email-regex %)))

(s/def ::created_at pos-int?)

(s/def ::updated_at pos-int?)

(s/def ::is_deleted boolean?)

;; valid
;; (s/conform ::id 111)
;; (s/conform ::id 001)
;; invalid
;; (s/conform ::id "1")
;; (s/conform ::id -1)
;; (s/conform ::id "test001")

;; valid
;; (s/conform ::name "testUser")
;; (s/conform ::name "test User")
;; (s/conform ::name "テストユーザ")
;; invalid
;; (s/conform ::name "t")
;; (s/conform ::name "thgoehoegehrothsopthgoghsoahfgss")
;; (s/conform ::name "testUser ")
;; (s/conform ::name "testUse%^$r")

;; valid
;; (s/conform ::email "meguru.mokke@gmail.com")
;; (s/conform ::email "hoge@cs.tsukuba.ac.jp")
;; invalid
;; (s/conform ::email "meguru.mokkegmail.com")

;; valid
;; (s/conform ::password "abC120aD")
;; invalid
;; (s/conform ::password "ahogea")
;; (s/conform ::password "ahogeafasdtqsahgoashothof12321DAFsd") 
;; (s/conform ::password "パスワード")
;; (s/conform ::password "pas10P　")


;; comment for service
;; (def num-big-little-char
;;   (map char (concat
;;               (range 97 123)
;;               (range 65 91)
;;               (range 48 58))))

;; (def num-big-little-char-len
;;   (count num-big-little-char))

;; (defn random-char []
;;   (nth num-big-little-char (rand num-big-little-char-len)))

;; (defn random-token
;;   ([]
;;    (random-token 127))
;;   ([length]

;;    (apply str (take length (repeatedly random-char)))))


;; get user-tokens
;; user id
;; tokens
;; apply and map = generated token tokens
