package io.llambda.compiler.planner
import io.llambda

import llambda.compiler.planner.{step => ps}
import llambda.compiler.planner.{intermediatevalue => iv}
import llambda.compiler.{StorageLocation, ContextLocated}
import llambda.compiler.et
import llambda.compiler.{valuetype => vt}

import scala.collection.breakOut

private[planner] object AttemptInlineApply {
  def apply(parentState : PlannerState, inlineState : PlannerState)(lambdaExpr : et.Lambda, operands : List[(ContextLocated, iv.IntermediateValue)])(implicit plan : PlanWriter) : Option[ResultValues] = {
    val mutableVars = plan.config.analysis.mutableVars
    val allArgs = lambdaExpr.fixedArgs ++ lambdaExpr.restArgOpt

    if (!(mutableVars & allArgs.toSet).isEmpty) {
      // Not supported yet
      return None
    }

    if ((operands.length < lambdaExpr.fixedArgs.length) ||
        ((operands.length > lambdaExpr.fixedArgs.length) && !lambdaExpr.restArgOpt.isDefined)) {
      // Incompatible arity - let PlanInvokeApply fail this
      return None
    }

    if (ContainsImmediateReturn(lambdaExpr.body)) {
      // Not supported yet
      return None
    }
    
    val closedVars = FindClosedVars(parentState, lambdaExpr.body, None)

    val importedValues = closedVars collect {
      case ImportedImmutable(storageLoc, parentIntermediate) =>
        (storageLoc -> ImmutableValue(parentIntermediate))

      case commonCapture : CapturedVariable if inlineState.values.contains(commonCapture.storageLoc) =>
        // This is captured variable the lambda expression and our inline state have in common
        // We can just import it directly for the purposes of inlining
        (commonCapture.storageLoc -> inlineState.values(commonCapture.storageLoc))

      case _ =>
        // Not supported
        return None
    }

    // Convert our arguments to ImmutableValues
    val fixedArgImmutables = (lambdaExpr.fixedArgs.zip(operands).map { case (storageLoc, (_, argValue)) =>
      if (vt.SatisfiesType(storageLoc.schemeType, argValue.schemeType) != Some(true)) {
        // This type cast could fail at runtime
        return None
      }

      (storageLoc -> ImmutableValue(argValue))
    })(breakOut) : Map[StorageLocation, LocationValue]
    
    val restArgImmutables = lambdaExpr.restArgOpt.zip(lambdaExpr.schemeType.restArgMemberTypeOpt) map {
      case (storageLoc, memberType) =>
        val restValues = operands.drop(lambdaExpr.fixedArgs.length).map(_._2)
        
        for (restValue <- restValues)  {
          if (vt.SatisfiesType(memberType, restValue.schemeType) != Some(true)) {
            // This type cast could fail at runtime
            return None
          }
        }

        storageLoc -> ImmutableValue(ValuesToList(restValues))
    }

    // Map our input immutables to their new storage locations
    val inlineBodyState = PlannerState(
      values=fixedArgImmutables ++ restArgImmutables ++ importedValues,
      inlineDepth=inlineState.inlineDepth + 1
    )

    val planResult = PlanExpr(inlineBodyState)(lambdaExpr.body)
    Some(planResult.values)
  }
}
