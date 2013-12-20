if exists("b:current_syntax")
  finish
endif

" Treat all bodies as the same
syn region celldefBodyBlock start="{" end="}" fold transparent contains=celldefBodyKeyword, celldefIntType, celldefFloatType, celldefLineComment, celldefCommentBlock

syn keyword celldefTopLevelKeyword root abstract concrete internal preconstructed cell fieldtype typetag
syn keyword celldefBodyKeyword     cppname extern contained

syn match   celldefIntType    /\<u\?int\d\+\>/ 
syn keyword celldefFloatType float double bool untypedptr

syn match   celldefLineComment   /\/\/.*/
syn region  celldefCommentBlock  start="/\*" end="\*/"

hi def link celldefTopLevelKeyword Keyword 
hi def link celldefBodyKeyword     Keyword 
hi def link celldefIntType         Type
hi def link celldefFloatType       Type
hi def link celldefLineComment     Comment
hi def link celldefCommentBlock    Comment

let b:current_syntax = "celldef"
