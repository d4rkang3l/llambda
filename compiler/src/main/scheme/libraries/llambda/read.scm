(define-library (llambda read)
  (import (llambda nfi))
  (import (rename (llambda internal primitives) (define-stdlib-procedure define-stdlib)))
  (import (only (scheme base) current-input-port))

  (include-library-declarations "../../interfaces/scheme/read.scm")
  (export <readable>)

  (begin
    (define-type <readable> (U <pair> <empty-list> <string> <symbol> <boolean> <number> <char> <vector> <bytevector>
                               <unit>))

    (define-native-library llread (static-library "ll_scheme_read"))

    (define native-read (world-function llread "llread_read" (-> <port> (U <readable> <eof-object>))))
    (define-stdlib (read [port : <port> (current-input-port)])
                 (native-read port))))
