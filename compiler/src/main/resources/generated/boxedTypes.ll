;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; This file is generated by gen-types.py. Do not edit manually. ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; {unsigned typeId, unsigned gcState}
%boxedValue = type {i16, i16}

; {supertype}
%unspecific = type {%boxedValue}

; {supertype, car, cdr}
%pair = type {%boxedValue, %boxedValue*, %boxedValue*}

; {supertype}
%emptyList = type {%boxedValue}

; {supertype, unsigned byteLength, utf8Data}
%stringLike = type {%boxedValue, i32, i8*}

; {supertype}
%string = type {%stringLike}

; {supertype}
%symbol = type {%stringLike}

; {supertype, value}
%boolean = type {%boxedValue, i8}

; {supertype, signed value}
%exactInteger = type {%boxedValue, i64}

; {supertype, value}
%inexactRational = type {%boxedValue, double}

; {supertype, unsigned codePoint}
%character = type {%boxedValue, i32}

; {supertype, unsigned length, data}
%byteVector = type {%boxedValue, i32, i8*}

; {supertype, closure, entryPoint}
%procedure = type {%boxedValue, %closure*, %boxedValue* (%closure*, %boxedValue*)*}

; {supertype, unsigned length, elements}
%vectorLike = type {%boxedValue, i32, %boxedValue**}

; {supertype}
%vector = type {%vectorLike}

; {supertype}
%closure = type {%vectorLike}
