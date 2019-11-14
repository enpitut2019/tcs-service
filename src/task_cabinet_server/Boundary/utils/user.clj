(ns task-cabinet-server.Boundary.utils.user)

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
;;;;;;;;;;;;;;;;;;;;;;;;;
