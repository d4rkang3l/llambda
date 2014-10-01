(define-test "simple (case-lambda)" (expect ((0 1 2) . (3 4))
 (import (scheme case-lambda))

 (define range
   (case-lambda
     ((e) (range 0 e))
     ((b e) (do ((r '() (cons e r))
                 (e (- e 1) (- e 1)))
              ((< e b) r)))))
 (cons (range 3) (range 3 5))))

(define-test "simple (case-lambda) using R7RS definition" (expect ((0 1 2) . (3 4))
  (import (llambda r7rs-case-lambda))
  (define range
    (r7rs-case-lambda
      ((e) (range 0 e))
      ((b e) (do ((r '() (cons e r))
                  (e (- e 1) (- e 1)))
               ((< e b) r)))))
  (cons (range 3) (range 3 5))))

(define-test "(case-lambda) with rest args" (expect (2 3 4)
  (import (scheme case-lambda))

  (define rest-lambda
    (case-lambda
      ((first) 'first)
      ((first second) 'second)
      ((first second . rest) rest)))
  (rest-lambda 0 1 2 3 4)))

(define-test "(case-lambda) with wrong arity fails at compile time" (expect-compile-failure
  (import (scheme case-lambda))

  (define fixed-lambda
    (case-lambda
      ((first) 'first)
      ((first second) 'second)))
  (fixed-lambda 0 1 2)))

(define-test "(case-lambda:) with type fails at compile time" (expect-compile-failure
  (import (scheme case-lambda))
  (import (llambda typed))

  (define fixed-lambda
    (case-lambda:
      (((first : <exact-integer>)) 'first)
      (((first : <exact-integer>) (second : <symbol>)) 'second)))
  (fixed-lambda 0 1)))

(define-test "(case-lambda:) returns value with case-> type" (expect-success
  (import (scheme case-lambda))
  (import (llambda typed))

  (define fixed-lambda
    (case-lambda:
      (((first : <exact-integer>)) 'first)
      (((first : <exact-integer>) (second : <symbol>)) 'second)))

  (ann fixed-lambda (case-> (-> <exact-integer> *) (-> <exact-integer> <symbol> *)))))

(define-test "(case-lambda:) cannot be annotated with incompatible case-> type" (expect-compile-failure
  (import (scheme case-lambda))
  (import (llambda typed))

  (define fixed-lambda
    (case-lambda:
      (((first : <exact-integer>)) 'first)
      (((first : <exact-integer>) (second : <symbol>)) 'second)))

  (ann fixed-lambda (case-> (-> <exact-integer> *) (-> <exact-integer> <string> *)))))

(define-test "R7RS (case-lambda) with wrong arity fails at runtime" (expect-failure
  (import (llambda r7rs-case-lambda))

  (define fixed-lambda
    (r7rs-case-lambda
      ((first) 'first)
      ((first second) 'second)))
  (fixed-lambda 0 1 2)))
