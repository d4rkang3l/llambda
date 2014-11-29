package io.llambda.compiler.planner
import io.llambda

import llambda.compiler.planner.{step => ps}
import llambda.compiler.planner.{intermediatevalue => iv}
import llambda.compiler.{ContextLocated, RuntimeErrorMessage}
import llambda.compiler.{valuetype => vt}
import llambda.compiler.{celltype => ct}

import llambda.compiler.valuetype.Implicits._

object PlanInvokeApply {
  def withTempValues(
      invokableProc : InvokableProcedure,
      fixedTemps : Seq[ps.TempValue],
      restTemps : Option[ps.TempValue]
  )(implicit plan : PlanWriter) : ResultValues = {
    val entryPointTemp = invokableProc.planEntryPoint()
    val signature = invokableProc.polySignature.upperBound

    val worldTemps = if (signature.hasWorldArg) {
      List(ps.WorldPtrValue)
    }
    else {
      Nil
    }

    val selfTemps = if (signature.hasSelfArg) {
      List(invokableProc.planSelf())
    }
    else {
      Nil
    }
    
    val argTemps = worldTemps ++ selfTemps ++ fixedTemps ++ restTemps

    signature.returnType match {
      case vt.ReturnType.SingleValue(vt.UnitType) =>
        plan.steps += ps.Invoke(None, signature, entryPointTemp, argTemps)
        SingleValue(iv.UnitValue)

      case otherType =>
        val resultTemp = ps.Temp(otherType.representationTypeOpt.get)
        plan.steps += ps.Invoke(Some(resultTemp), signature, entryPointTemp, argTemps)

        TempValueToResults(otherType, resultTemp)
    }
  }

  def withArgumentList(
      invokableProc : InvokableProcedure,
      argListValue : iv.IntermediateValue
  )(implicit plan : PlanWriter) : ResultValues = {
    val signature = invokableProc.polySignature.upperBound

    val insufficientArgsMessage = ArityRuntimeErrorMessage.insufficientArgs(invokableProc)

    // Split our arguments in to fixed args and a rest arg
    val destructuredArgs = DestructureList(argListValue, signature.fixedArgTypes, insufficientArgsMessage)

    val fixedArgTemps = destructuredArgs.memberTemps
    val restArgValue = destructuredArgs.listTailValue

    val restArgTemps = signature.restArgMemberTypeOpt match {
      case Some(memberType) =>
        val requiredRestArgType = vt.UniformProperListType(memberType)
        val typeCheckedRestArg = restArgValue.toTempValue(requiredRestArgType)

        Some(typeCheckedRestArg)

      case None =>
        val tooManyArgsMessage = ArityRuntimeErrorMessage.tooManyArgs(invokableProc)
        
        // Make sure we're out of args by doing a check cast to an empty list
        restArgValue.toTempValue(vt.EmptyListType, Some(tooManyArgsMessage))
        None
    }

    PlanInvokeApply.withTempValues(invokableProc, fixedArgTemps, restArgTemps) 
  }

  def withIntermediateValues(
      invokableProc : InvokableProcedure,
      operands : List[(ContextLocated, iv.IntermediateValue)]
  )(implicit plan : PlanWriter) : ResultValues = {
    val polySignature = invokableProc.polySignature

    // Instantiate our polymorphic signature
    val operandTypes = operands.map(_._2.schemeType)
    val signature = polySignature.signatureForOperands(plan.activeContextLocated, operandTypes)

    // Convert all the operands
    val fixedTemps = operands.zip(signature.fixedArgTypes) map { case ((contextLocated, operand), nativeType) =>
      plan.withContextLocation(contextLocated) {
        operand.toTempValue(nativeType)
      }
    }

    val restTemps = signature.restArgMemberTypeOpt map { memberType =>
      val restOperands = operands.drop(signature.fixedArgTypes.length)

      val restArgs = restOperands map { case (contextLocated, restValue) =>
        plan.withContextLocation(contextLocated) {
          restValue.castToSchemeType(memberType)
        }
      }

      ValuesToList(restArgs, capturable=false).toTempValue(vt.ListElementType)
    }

    PlanInvokeApply.withTempValues(invokableProc, fixedTemps, restTemps)
  }
}

