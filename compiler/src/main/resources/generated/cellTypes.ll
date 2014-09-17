;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; This file is generated by typegen. Do not edit manually. ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; {supertype, typeId, gcState}
%any = type {i8, i8}
!10 = metadata !{metadata !"Any::typeId"}
!11 = metadata !{metadata !"Any::gcState"}

; {supertype}
%unit = type {%any}
!12 = metadata !{metadata !"Any::typeId->Unit", metadata !10}
!13 = metadata !{metadata !"Any::gcState->Unit", metadata !11}

; {supertype}
%listElement = type {%any}
!14 = metadata !{metadata !"Any::typeId->ListElement", metadata !10}
!15 = metadata !{metadata !"Any::gcState->ListElement", metadata !11}

; {supertype, unsigned listLength, car, cdr}
%pair = type {%listElement, i32, %any*, %any*}
!16 = metadata !{metadata !"Any::typeId->ListElement->Pair", metadata !14}
!17 = metadata !{metadata !"Any::gcState->ListElement->Pair", metadata !15}
!18 = metadata !{metadata !"Pair::listLength"}
!19 = metadata !{metadata !"Pair::car"}
!20 = metadata !{metadata !"Pair::cdr"}

; {supertype}
%emptyList = type {%listElement}
!21 = metadata !{metadata !"Any::typeId->ListElement->EmptyList", metadata !14}
!22 = metadata !{metadata !"Any::gcState->ListElement->EmptyList", metadata !15}

; {supertype, unsigned allocSlackBytes, unsigned charLength, unsigned byteLength}
%string = type {%any, i16, i32, i32}
!23 = metadata !{metadata !"Any::typeId->String", metadata !10}
!24 = metadata !{metadata !"Any::gcState->String", metadata !11}
!25 = metadata !{metadata !"String::allocSlackBytes"}
!26 = metadata !{metadata !"String::charLength"}
!27 = metadata !{metadata !"String::byteLength"}

; {supertype, inlineData}
%inlineString = type {%string, [12 x i8]}
!28 = metadata !{metadata !"InlineString::inlineData"}

; {supertype, heapByteArray}
%heapString = type {%string, %sharedByteArray*}
!29 = metadata !{metadata !"HeapString::heapByteArray"}

; {supertype, unsigned charLength, unsigned byteLength}
%symbol = type {%any, i32, i32}
!30 = metadata !{metadata !"Any::typeId->Symbol", metadata !10}
!31 = metadata !{metadata !"Any::gcState->Symbol", metadata !11}
!32 = metadata !{metadata !"Symbol::charLength"}
!33 = metadata !{metadata !"Symbol::byteLength"}

; {supertype, inlineData}
%inlineSymbol = type {%symbol, [12 x i8]}
!34 = metadata !{metadata !"InlineSymbol::inlineData"}

; {supertype, heapByteArray}
%heapSymbol = type {%symbol, %sharedByteArray*}
!35 = metadata !{metadata !"HeapSymbol::heapByteArray"}

; {supertype, bool value}
%boolean = type {%any, i8}
!36 = metadata !{metadata !"Any::typeId->Boolean", metadata !10}
!37 = metadata !{metadata !"Any::gcState->Boolean", metadata !11}
!38 = metadata !{metadata !"Boolean::value"}

; {supertype}
%number = type {%any}
!39 = metadata !{metadata !"Any::typeId->Number", metadata !10}
!40 = metadata !{metadata !"Any::gcState->Number", metadata !11}

; {supertype, signed value}
%exactInteger = type {%number, i64}
!41 = metadata !{metadata !"Any::typeId->Number->ExactInteger", metadata !39}
!42 = metadata !{metadata !"Any::gcState->Number->ExactInteger", metadata !40}
!43 = metadata !{metadata !"ExactInteger::value"}

; {supertype, value}
%flonum = type {%number, double}
!44 = metadata !{metadata !"Any::typeId->Number->Flonum", metadata !39}
!45 = metadata !{metadata !"Any::gcState->Number->Flonum", metadata !40}
!46 = metadata !{metadata !"Flonum::value"}

; {supertype, unicodeChar}
%char = type {%any, i32}
!47 = metadata !{metadata !"Any::typeId->Char", metadata !10}
!48 = metadata !{metadata !"Any::gcState->Char", metadata !11}
!49 = metadata !{metadata !"Char::unicodeChar"}

; {supertype, unsigned length, elements}
%vector = type {%any, i32, %any**}
!50 = metadata !{metadata !"Any::typeId->Vector", metadata !10}
!51 = metadata !{metadata !"Any::gcState->Vector", metadata !11}
!52 = metadata !{metadata !"Vector::length"}
!53 = metadata !{metadata !"Vector::elements"}

; {supertype, unsigned length, byteArray}
%bytevector = type {%any, i32, %sharedByteArray*}
!54 = metadata !{metadata !"Any::typeId->Bytevector", metadata !10}
!55 = metadata !{metadata !"Any::gcState->Bytevector", metadata !11}
!56 = metadata !{metadata !"Bytevector::length"}
!57 = metadata !{metadata !"Bytevector::byteArray"}

; {supertype, bool dataIsInline, bool isUndefined, unsigned recordClassId, recordData}
%recordLike = type {%any, i8, i8, i32, i8*}
!58 = metadata !{metadata !"Any::typeId->RecordLike", metadata !10}
!59 = metadata !{metadata !"Any::gcState->RecordLike", metadata !11}
!60 = metadata !{metadata !"RecordLike::dataIsInline"}
!61 = metadata !{metadata !"RecordLike::isUndefined"}
!62 = metadata !{metadata !"RecordLike::recordClassId"}
!63 = metadata !{metadata !"RecordLike::recordData"}

; {supertype, entryPoint}
%procedure = type {%recordLike, i8*}
!64 = metadata !{metadata !"Any::typeId->RecordLike->Procedure", metadata !58}
!65 = metadata !{metadata !"Any::gcState->RecordLike->Procedure", metadata !59}
!66 = metadata !{metadata !"RecordLike::dataIsInline->Procedure", metadata !60}
!67 = metadata !{metadata !"RecordLike::isUndefined->Procedure", metadata !61}
!68 = metadata !{metadata !"RecordLike::recordClassId->Procedure", metadata !62}
!69 = metadata !{metadata !"RecordLike::recordData->Procedure", metadata !63}
!70 = metadata !{metadata !"Procedure::entryPoint"}

; {supertype, extraData}
%record = type {%recordLike, i8*}
!71 = metadata !{metadata !"Any::typeId->RecordLike->Record", metadata !58}
!72 = metadata !{metadata !"Any::gcState->RecordLike->Record", metadata !59}
!73 = metadata !{metadata !"RecordLike::dataIsInline->Record", metadata !60}
!74 = metadata !{metadata !"RecordLike::isUndefined->Record", metadata !61}
!75 = metadata !{metadata !"RecordLike::recordClassId->Record", metadata !62}
!76 = metadata !{metadata !"RecordLike::recordData->Record", metadata !63}
!77 = metadata !{metadata !"Record::extraData"}

; {supertype, message, irritants}
%errorObject = type {%any, %string*, %listElement*}
!78 = metadata !{metadata !"Any::typeId->ErrorObject", metadata !10}
!79 = metadata !{metadata !"Any::gcState->ErrorObject", metadata !11}
!80 = metadata !{metadata !"ErrorObject::message"}
!81 = metadata !{metadata !"ErrorObject::irritants"}

; {supertype, bool isOwned, stream}
%port = type {%any, i8, i8*}
!82 = metadata !{metadata !"Any::typeId->Port", metadata !10}
!83 = metadata !{metadata !"Any::gcState->Port", metadata !11}
!84 = metadata !{metadata !"Port::isOwned"}
!85 = metadata !{metadata !"Port::stream"}
