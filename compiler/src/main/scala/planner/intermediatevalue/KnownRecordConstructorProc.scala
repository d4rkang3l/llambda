package io.llambda.compiler.planner.intermediatevalue
import io.llambda

import llambda.compiler.{ProcedureSignature, ContextLocated}
import llambda.compiler.planner._
import llambda.compiler.{valuetype => vt}
import llambda.compiler.planner.{step => ps}

class KnownRecordConstructorProc(recordType : vt.RecordType, initializedFields : List[vt.RecordField]) extends KnownArtificialProc(
    ProcedureSignature(
      hasWorldArg=true,
      hasSelfArg=false,
      restArgMemberTypeOpt=None,
      fixedArgTypes=initializedFields.map(_.fieldType),
      returnType=vt.ReturnType.SingleValue(recordType),
      attributes=Set()
    )
){
  protected val symbolHint =
    recordType.sourceName
      .replaceAllLiterally("<", "")
      .replaceAllLiterally(">", "")

  def planFunction(parentPlan : PlanWriter, allocedSymbol : String) : PlannedFunction = {
    val plan = parentPlan.forkPlan()

    val fieldToTempValue = (initializedFields.map { field =>
      (field, ps.Temp(field.fieldType))
    }).toMap

    // Get unique argument names
    val argumentUniquer = new SourceNameUniquer

    val namedArguments = ("world" -> ps.WorldPtrValue) ::
      (initializedFields.map { case field =>
        (argumentUniquer(field.sourceName) -> fieldToTempValue(field))
      }).toList

    // Initialize the record
    val cellTemp = ps.RecordTemp()
    val dataTemp = ps.RecordLikeDataTemp()

    plan.steps += ps.InitRecordLike(cellTemp, dataTemp, recordType, isUndefined=false)

    // Set all our fields
    for(field <- recordType.fields) {
      val fieldTemp = fieldToTempValue.getOrElse(field, {
        UnitValue.toTempValue(field.fieldType)(plan)
      })

      plan.steps += ps.SetRecordDataField(dataTemp, recordType, field, fieldTemp)
    }

    // Return the record
    plan.steps += ps.Return(Some(cellTemp))

    PlannedFunction(
      signature=signature,
      namedArguments=namedArguments,
      steps=plan.steps.toList,
      debugContextOpt=None
    )
  }

  override def attemptInlineApplication(state : PlannerState)(operands : List[(ContextLocated, IntermediateValue)])(implicit plan : PlanWriter) : Option[PlanResult] = {
    if (operands.length != initializedFields.length) {
      // Not the right number of operands
      return None
    }

    // Convert our fields to the correct values
    val fieldToTempValue = (operands.map(_._2).zip(initializedFields) map { case (operandValue, field) =>
      (field -> operandValue.toTempValue(field.fieldType))
    }).toMap

    // Build the record value
    val cellTemp = ps.RecordTemp()
    val dataTemp = ps.RecordLikeDataTemp()

    plan.steps += ps.InitRecordLike(cellTemp, dataTemp, recordType, isUndefined=false)

    for(field <- recordType.fields) {
      val fieldTemp = fieldToTempValue.getOrElse(field, {
        UnitValue.toTempValue(field.fieldType)(plan)
      })
        
      plan.steps += ps.SetRecordDataField(dataTemp, recordType, field, fieldTemp)
    }

    val resultValue = TempValueToIntermediate(recordType, cellTemp)(plan.config)

    Some(PlanResult(
      state=state,
      values=SingleValue(resultValue)
    ))
  }
}
