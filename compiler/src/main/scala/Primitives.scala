package io.llambda.compiler
import io.llambda

/** Bindings for the primitive expressions and type constructors */
object Primitives {
  object Begin extends PrimitiveExpr
  object Lambda extends PrimitiveExpr
  object CaseLambda extends PrimitiveExpr
  object Quote extends PrimitiveExpr
  object If extends PrimitiveExpr
  object Set extends PrimitiveExpr
  object SyntaxError extends PrimitiveExpr
  object Include extends PrimitiveExpr
  object IncludeCI extends PrimitiveExpr
  object Quasiquote extends PrimitiveExpr
  object Unquote extends PrimitiveExpr
  object UnquoteSplicing extends PrimitiveExpr
  object Define extends PrimitiveExpr
  object DefineValues extends PrimitiveExpr
  object DefineSyntax extends PrimitiveExpr
  object DefineRecordType extends PrimitiveExpr
  object DefineType extends PrimitiveExpr
  object DefineReportProcedure extends PrimitiveExpr
  object Cast extends PrimitiveExpr
  object AnnotateExprType extends PrimitiveExpr
  object AnnotateStorageLocType extends PrimitiveExpr
  object CondExpand extends PrimitiveExpr
  object Parameterize extends PrimitiveExpr
  object MakePredicate extends PrimitiveExpr
  object Ellipsis extends PrimitiveExpr
  object Wildcard extends PrimitiveExpr
  object SyntaxRules extends PrimitiveExpr

  object DefineNativeLibrary extends PrimitiveExpr
  object StaticLibrary extends PrimitiveExpr
  object NativeFunction extends PrimitiveExpr
  object WorldFunction extends PrimitiveExpr
  object NoReturnAttr extends PrimitiveExpr

  object UnionType extends PrimitiveTypeConstructor
  object PairofType extends PrimitiveTypeConstructor
  object ListofType extends PrimitiveTypeConstructor
  object ListType extends PrimitiveTypeConstructor
  object RecType extends PrimitiveTypeConstructor
  object VectorofType extends PrimitiveTypeConstructor
  object VectorType extends PrimitiveTypeConstructor
  object ValuesType extends PrimitiveTypeConstructor
  object ProcedureType extends PrimitiveTypeConstructor
  object CaseProcedureType extends PrimitiveTypeConstructor
  object PolymorphicType extends PrimitiveTypeConstructor

  val bindings = {
    Map[String, BoundValue](
      "begin" -> Begin,
      "lambda" -> Lambda,
      "case-lambda" -> CaseLambda,
      "quote" -> Quote,
      "if" -> If,
      "set!" -> Set,
      "syntax-error" -> SyntaxError,
      "include" -> Include,
      "include-ci" -> IncludeCI,
      "quasiquote" -> Quasiquote,
      "unquote" -> Unquote,
      "unquote-splicing" -> UnquoteSplicing,
      "define" -> Define,
      "define-values" -> DefineValues,
      "define-syntax" -> DefineSyntax,
      "define-record-type" -> DefineRecordType,
      "define-type" -> DefineType,
      "define-report-procedure" -> DefineReportProcedure,
      "cast" -> Cast,
      "ann" -> AnnotateExprType,
      ":" -> AnnotateStorageLocType,
      "cond-expand" -> CondExpand,
      "parameterize" -> Parameterize,
      "make-predicate" -> MakePredicate,
      "..." -> Ellipsis,
      "_" -> Wildcard,
      "syntax-rules" -> SyntaxRules,

      "define-native-library" -> DefineNativeLibrary,
      "static-library" -> StaticLibrary,
      "native-function" -> NativeFunction,
      "world-function" -> WorldFunction,
      "noreturn" -> NoReturnAttr,

      "U" -> UnionType,
      "Pairof" -> PairofType,
      "Listof" -> ListofType,
      "List" -> ListType,
      "Rec" -> RecType,
      "Vectorof" -> VectorofType,
      "Vector" -> VectorType,
      "Values" -> ValuesType,
      "->" -> ProcedureType,
      "case->" -> CaseProcedureType,
      "All" -> PolymorphicType
    )
  }
}

