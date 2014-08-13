(define-test "(number?)" (expect-success
  (assert-true  (number? 4))
  (assert-true  (number? (typeless-cell 4)))

  (assert-true  (number? -5.0))
  (assert-true  (number? (typeless-cell -5.0)))

  (assert-false (number? '()))
  (assert-false (number? (typeless-cell '())))))

(define-test "(real?)" (expect-success
  (assert-true  (real? 4))
  (assert-true  (real? -5.0))
  (assert-false (real? '()))))

(define-test "(rational?)" (expect-success
  (assert-true  (rational? 4))
  (assert-true  (rational? -5.0))
  (assert-false (rational? '()))))

(define-test "(complex?)" (expect-success
  (assert-true  (complex? 4))
  (assert-true  (complex? -5.0))
  (assert-false (complex? '()))))

(define-test "(integer?)" (expect-success
  (assert-true  (integer? 4))
  (assert-true  (integer? -5.0))
  (assert-false (integer? -5.5))
  (assert-false (integer? +nan.0))
  (assert-false (integer? '()))))

(define-test "(exact?)" (expect-success
  (assert-false (exact? 3.0))
  (assert-true  (exact? 3.))))

(define-test "exact? fails with non-numbers" (expect-failure
  (exact? 'notanumber)))

(define-test "(inexact?)" (expect-success
  (assert-true  (inexact? 3.0))
  (assert-false (inexact? 3.))))

(define-test "(inexact?) fails with non-numbers" (expect-failure
  (inexact? 'notanumber)))

(define-test "(finite?)" (expect-success
  (assert-true  (finite? 3))
  (assert-true  (finite? 4.5))
  (assert-false (finite? +inf.0))
  (assert-false (finite? +nan.0))))

(define-test "(infinite?)" (expect-success
  (assert-false (infinite? 3))
  (assert-false (infinite? 4.5))
  (assert-true  (infinite? +inf.0))
  (assert-false (infinite? +nan.0))))

(define-test "(nan?)" (expect-success
  (assert-false (nan? 3))
  (assert-false (nan? 4.5))
  (assert-false (nan? +inf.0))
  (assert-true  (nan? +nan.0))))

(define-test "(exact-integer?)" (expect-success
  (assert-true (exact-integer? 32))
  (assert-false (exact-integer? 32.0))))

(define-test "(exact)" (expect-success
  (assert-equal -32 (exact -32.0))
  (assert-equal 64 (exact 64))))

(define-test "(exact 112.5) fails" (expect-failure
  (exact 112.5)))

(define-test "Inexact 567 is 567.0" (expect-success
  (assert-equal 567.0 (inexact 567))
  (assert-equal -3289.5 (inexact -3289.5))))

; This can't be exactly represented by a double
(define-test "(inexact 9007199254740993) fails" (expect-failure
  (inexact 9007199254740993)))

; Super ghetto but anything else depends too much on floating point
; representations
(define-test "inexact trigonometric procedures" (expect-success
  (import (scheme inexact))
  (assert-equal 0.0 (sin 0.0))
  (assert-equal 1.0 (cos 0.0))
  (assert-equal 0.0 (tan 0.0))))

(define-test "exact trigonometric procedures" (expect-success
  (import (scheme inexact))
  (assert-equal 0.0 (sin 0))
  (assert-equal 1.0 (cos 0))
  (assert-equal 0.0 (tan 0))))

(define-test "(+)" (expect-success
  (assert-equal 0 (+))
  (assert-equal 12 (+ 12))
  (assert-equal -450.5 (+ -450.5))
  (assert-equal -435065 (+ 70 -1024589 589454))
  (assert-equal 300.0 (+ 100.5 -0.5 200.0))
  (assert-equal 300.0 (+ 100.5 -0.5 200))
             
  (define dynamic-5 (length (typeless-cell '(1 2 3 4 5))))
  (assert-equal 8 (+ dynamic-5 1 2))))

(define-test "adding single string fails" (expect-failure
  (+ "Hello!")))

(define-test "(*)" (expect-success
  (assert-equal 1 (*))
  (assert-equal 12 (* 12))
  (assert-equal -450.5 (* -450.5))
  (assert-equal -499332738025 (* 4135 -3547 34045))
  (assert-equal -10050.0 (* 100.5 -0.5 200.0))
  (assert-equal 10050.0 (* 100.5 0.5 200))
  
  (define dynamic-5 (length (typeless-cell '(1 2 3 4 5))))
  (assert-equal 10 (* dynamic-5 1 2))))

(define-test "multiplying single string fails" (expect-failure
  (* "Hello!")))

(define-test "(-)" (expect-success
  (assert-equal -12 (- 12))
  (assert-equal 450.5 (- -450.5))
  (assert-equal -26363 (- 4135 -3547 34045))
  (assert-equal -99.0 (- 100.5 -0.5 200.0))
  (assert-equal -100.0 (- 100.5 0.5 200))
  
  (define dynamic-5 (length (typeless-cell '(1 2 3 4 5))))
  (assert-equal 2 (- dynamic-5 1 2))
  (assert-equal -6 (- 1 2 dynamic-5))))

(define-test "subtracting no numbers fails" (expect-failure
  (-)))

(define-test "subtracting single string fails" (expect-failure
  (- "Hello!")))

(define-test "(/)" (expect-success
  (assert-equal 0.125 (/ 8))
  (assert-equal -4.0 (/ -0.25))
  (assert-equal 0.15 (/ 3 4 5))
  (assert-equal 64.0 (/ 128.0 0.25 8))
  (assert-equal -64.0 (/ 128.0 -0.25 8))))

(define-test "dividing single string fails" (expect-failure
  (/ "Hello!")))

(define-test "dividing no numbers fails" (expect-failure
  (/)))

(define-test "(=)" (expect-success
  (assert-true  (= 4.0 4))
  (assert-true  (= 0.0 -0.0))
  (assert-true  (= 4.0 4 4.0))
  (assert-false (= 4.0 5.6))
  (assert-false (= 4.0 4 5.6))))

(define-test "equality of two numbers and boolean false is an error" (expect-failure
  (= 4.0 4 #f)))

(define-test "(<)" (expect-success
  (assert-false (< 4.0 4))
  (assert-false (< -0.0 0.0))
  (assert-false (< 4.0 4 4.0))
  (assert-false (< 5.6 4.0))
  (assert-false (< 5.6 0 -4.5))
  (assert-true  (< 4.0 5.6))
  (assert-true  (< 4.0 4.5 5.6))))

(define-test "(>)" (expect-success
  (assert-false (> 4.0 4))
  (assert-false (> -0.0 0.0))
  (assert-false (> 4.0 4 4.0))
  (assert-true  (> 5.6 4.0))
  (assert-true  (> 5.6 0 -4.5))
  (assert-false (> 4.0 5.6))
  (assert-false (> 4.0 4.5 5.6))))

(define-test "(<=)" (expect-success
  (assert-true  (<= 4.0 4))
  (assert-true  (<= -0.0 0.0))
  (assert-true  (<= 4.0 4 4.0))
  (assert-false (<= 5.6 4.0))
  (assert-false (<= 5.6 0 -4.5))
  (assert-true  (<= 4.0 5.6))
  (assert-true  (<= 4.0 4.5 5.6))))

(define-test "(>=)" (expect-success
  (assert-true  (>= 4.0 4))
  (assert-true  (>= -0.0 0.0))
  (assert-true  (>= 4.0 4 4.0))
  (assert-true  (>= 5.6 4.0))
  (assert-true  (>= 5.6 0 -4.5))
  (assert-false (>= 4.0 5.6))
  (assert-false (>= 4.0 4.5 5.6))))

(define-test "(zero?)" (expect-success
  (assert-true  (zero? 0))
  (assert-true  (zero? 0.0))
  (assert-false (zero? 34))
  (assert-false (zero? -134.5))
  (assert-false (zero? +inf.0))
  (assert-false (zero? -inf.0))
  (assert-false (zero? +nan.0))))

(define-test "(even?)" (expect-success
  (assert-true  (even? 1024))
  (assert-false (even? 777))
  (assert-true  (even? 0))
  (assert-true  (even? -1024))
  (assert-false (even? -777))))

(define-test "(odd?)" (expect-success
  (assert-false (odd? 1024))
  (assert-true  (odd? 777))
  (assert-false (odd? 0))
  (assert-false (odd? -1024))
  (assert-true  (odd? -777))))

(define-test "(positive?)" (expect-success
  (assert-false (positive? +nan.0))
  (assert-false (positive? 0))
  (assert-false (positive? 0.0))
  (assert-true  (positive? +inf.0))
  (assert-false (positive? -inf.0))
  (assert-true  (positive? 35))
  (assert-true  (positive? 456.7))
  (assert-false (positive? -35))
  (assert-false (positive? -456.7))))

(define-test "(negative?)" (expect-success
  (assert-false (negative? +nan.0))
  (assert-false (negative? 0))
  (assert-false (negative? 0.0))
  (assert-false (negative? +inf.0))
  (assert-true  (negative? -inf.0))
  (assert-false (negative? 35))
  (assert-false (negative? 456.7))
  (assert-true  (negative? -35))
  (assert-true  (negative? -456.7))))

(define-test "rounding procedures" (expect-success
  (assert-equal -5.0 (floor -4.3))
  (assert-equal -4.0 (ceiling -4.3))
  (assert-equal -4.0 (truncate -4.3))
  (assert-equal -4.0 (round -4.3))
  (assert-equal 3.0  (floor 3.5))
  (assert-equal 4.0  (ceiling 3.5))
  (assert-equal 3.0  (truncate 3.5))
  (assert-equal 4.0  (round 3.5))
  (assert-equal 7    (round 7))))
