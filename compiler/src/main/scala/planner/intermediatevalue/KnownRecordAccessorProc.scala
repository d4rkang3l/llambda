package io.llambda.compiler.planner.intermediatevalue
import io.llambda

import llambda.compiler.{ProcedureSignature, ContextLocated}
import llambda.compiler.planner._
import llambda.compiler.{valuetype => vt}
import llambda.compiler.planner.{step => ps}

class KnownRecordAccessorProc(recordType: vt.RecordType, field: vt.RecordField) extends KnownArtificialProc(
    ProcedureSignature(
      hasWorldArg=false,
      hasSelfArg=false,
      mandatoryArgTypes=List(recordType),
      optionalArgTypes=Nil,
      restArgMemberTypeOpt=None,
      returnType=vt.ReturnType.Reachable(recordType.typeForField(field)),
      attributes=Set()
    ).toPolymorphic
) {
  protected val symbolHint =
    recordType.sourceName
      .replaceAllLiterally("<", "")
      .replaceAllLiterally(">", "") +
      "-" + field.name

  def planFunction(parentPlan: PlanWriter, allocedSymbol: String): PlannedFunction = {
    val recordCellTemp = ps.TempValue()

    val plan = parentPlan.forkPlan()

    // Read the field
    val fieldValueTemp = ps.TempValue()
    val fieldsToLoad = List((field -> fieldValueTemp))

    plan.steps += ps.LoadRecordLikeFields(recordCellTemp, recordType, fieldsToLoad)
    plan.steps += ps.Return(Some(fieldValueTemp))

    PlannedFunction(
      signature=polySignature.upperBound,
      namedArguments=List(("recordCell" -> recordCellTemp)),
      steps=plan.steps.toList,
      debugContextOpt=None
    )
  }

  override def attemptInlineApplication(state: PlannerState)(
      args: List[(ContextLocated, IntermediateValue)]
  )(implicit plan: PlanWriter): Option[PlanResult] = {
    args match {
      case List((_, knownRecord: KnownRecord))
          if vt.SatisfiesType(recordType, knownRecord.schemeType) == Some(true) && knownRecord.knownFieldValues.contains(field) =>
        val fieldValue = knownRecord.knownFieldValues(field)

        Some(PlanResult(
          state=state,
          value=fieldValue
        ))

      case List((_, recordValue)) =>
        val recordCellTemp = recordValue.toTempValue(recordType)

        // Read the field
        val fieldType = recordType.typeForField(field)
        val fieldValueTemp = ps.TempValue()
        val fieldsToLoad = List((field -> fieldValueTemp))

        plan.steps += ps.LoadRecordLikeFields(recordCellTemp, recordType, fieldsToLoad)

        val resultValue = TempValueToIntermediate(fieldType, fieldValueTemp)

        Some(PlanResult(
          state=state,
          value=resultValue
        ))

      case _ =>
        None
    }
  }
}
