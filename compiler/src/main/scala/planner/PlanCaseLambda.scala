package io.llambda.compiler.planner
import io.llambda

import llambda.compiler.et
import llambda.compiler.planner.{step => ps}
import llambda.compiler.{celltype => ct}
import llambda.compiler.{valuetype => vt}
import llambda.compiler.planner.{intermediatevalue => iv}
import llambda.compiler.{RuntimeErrorMessage, ProcedureSignature, ProcedureAttribute}

import llambda.compiler.valuetype.Implicits._

private[planner] object PlanCaseLambda {
  val noMatchingClauseRuntimeErrorMessage = RuntimeErrorMessage(
    name="noMatchingCaseLambdaClause",
    text="No matching (case-lambda) clause"
  )

  private case class CapturedProc(
      outerProcTemp : ps.TempValue,
      recordField : vt.RecordField
  )

  private case class PlannedClause(
      procValue : iv.KnownSchemeProc,
      capturedProcOpt : Option[CapturedProc]
  )

  private def listOfAtLeast(length : Int) : vt.SchemeType = {
    val anyList = vt.UniformProperListType(vt.AnySchemeType) 

    (0 until length).foldRight(anyList : vt.SchemeType) {
      case (_, cdrType) =>
        vt.SpecificPairType(vt.AnySchemeType, cdrType)
    }
  }
  
  private def listOfExactly(length : Int) : vt.SchemeType = {
    (0 until length).foldRight(vt.EmptyListType : vt.SchemeType) {
      case (_, cdrType) =>
        vt.SpecificPairType(vt.AnySchemeType, cdrType)
    }
  }

  private def planClauseTests(
      plannedClauses : List[PlannedClause],
      innerSelfTempOpt : Option[ps.TempValue],
      closureType : vt.ClosureType,
      argListHeadTemp : ps.TempValue,
      argLengthTemp : ps.TempValue
  )(implicit entryPlan : PlanWriter, worldPtrTemp : ps.WorldPtrValue) : ps.TempValue = plannedClauses match {
    case Nil =>
      // No clauses left - we fell off the end
      val falsePredTemp = ps.Temp(vt.Predicate)
      entryPlan.steps += ps.CreateNativeInteger(falsePredTemp, value=0, bits=vt.Predicate.bits)
      entryPlan.steps += ps.AssertPredicate(worldPtrTemp, falsePredTemp, noMatchingClauseRuntimeErrorMessage)

      // This isn't reachable but we need to return to make codegen happy
      // XXX: Communicate the unreachability to codegen
      iv.EmptyListValue.toTempValue(vt.ListElementType)(entryPlan, worldPtrTemp)

    case checkingClause :: tailClauses =>
      // See if our length matched
      val procValue = checkingClause.procValue
      val signature = procValue.signature
      val testingLength = signature.fixedArgTypes.length
      
      val testingLengthTemp = ps.Temp(vt.UInt32)
      entryPlan.steps += ps.CreateNativeInteger(testingLengthTemp, testingLength, vt.UInt32.bits)

      val (testingCond, signedOpt, argListType) = if (signature.restArgMemberTypeOpt.isDefined) {
        // We have a rest argument - we can deal with any args >= our fixed arg count
        (ps.CompareCond.GreaterThanEqual, Some(false), listOfAtLeast(testingLength))
      }
      else {
        // We can only match our fixed our count exactly
        (ps.CompareCond.Equal, None, listOfExactly(testingLength))
      }

      val matchesPred = ps.Temp(vt.Predicate)
      entryPlan.steps += ps.IntegerCompare(matchesPred, testingCond, signedOpt, argLengthTemp, testingLengthTemp) 

      entryPlan.buildCondBranch(matchesPred, { truePlan =>
        val restoredProcValue = checkingClause.capturedProcOpt match {
          case Some(CapturedProc(_, recordField)) =>
            // We need to restore this procedure from our closure
            val closureDataTemp = ps.RecordLikeDataTemp()
            truePlan.steps += ps.LoadRecordLikeData(closureDataTemp, innerSelfTempOpt.get, closureType)

            val restoredTemp = ps.Temp(recordField.fieldType)
            truePlan.steps += ps.LoadRecordDataField(restoredTemp, closureDataTemp, closureType, recordField)

            procValue.withSelfTemp(restoredTemp)

          case None =>
            procValue
        }

        val argListBoxed = BoxedValue(ct.ListElementCell, argListHeadTemp)
        val argListValue = new iv.CellValue(argListType, argListBoxed)

        val resultValues = PlanInvokeApply.withArgumentList(restoredProcValue, argListValue)(truePlan, worldPtrTemp)
         
        resultValues.toReturnTempValue(vt.ReturnType.ArbitraryValues)(truePlan, worldPtrTemp).get
      }, { falsePlan =>
        planClauseTests(
          plannedClauses=tailClauses,
          innerSelfTempOpt=innerSelfTempOpt,
          closureType=closureType,
          argListHeadTemp=argListHeadTemp,
          argLengthTemp=argLengthTemp
        )(falsePlan, worldPtrTemp)
      })
  }

  def apply(parentState : PlannerState, parentPlan : PlanWriter)(
      clauseExprs : List[et.Lambda],
      sourceNameHint : Option[String]
  ) : iv.KnownCaseLambdaProc = {
    val sourceName = sourceNameHint.getOrElse("anonymous-case-lambda")
    val nativeSymbol = parentPlan.allocSymbol(sourceName)

    // Plan each inner lambda clause
    val plannedClauses = clauseExprs.zipWithIndex.map { case (clauseExpr, index) =>
      val clauseNameHint = s"${sourceName} clause ${index}"

      val procValue = PlanLambda(parentState, parentPlan)(clauseExpr, Some(clauseNameHint), None)
      
      val capturedProcOpt = if (procValue.needsClosureRepresentation) {
        // Convert to a TempValue now because we can't enter GC while building our closure
        val procType = procValue.schemeType
        val procTemp = procValue.toTempValue(procType, convertProcType=false)(parentPlan, parentState.worldPtr)

        val recordField = new vt.RecordField(s"clause{$index}", procType)

        Some(CapturedProc(procTemp, recordField))
      }
      else {
        None
      }

      PlannedClause(procValue, capturedProcOpt)
    }

    // Determine if we need a closure
    val closureRequired = plannedClauses.exists(_.capturedProcOpt.isDefined)

    // Make our closure type
    val closureType = if (closureRequired) {
      val closureSourceName = sourceName + "-closure"

      val allFields = plannedClauses.flatMap(_.capturedProcOpt).map(_.recordField)
      new vt.ClosureType(closureSourceName, allFields)
    }
    else {
      vt.EmptyClosureType
    }
    
    val worldPtrTemp = new ps.WorldPtrValue

    val innerSelfTempOpt = if (closureRequired) {
      Some(ps.CellTemp(ct.ProcedureCell))
    }
    else {
      None
    }

    val argListHeadTemp = ps.CellTemp(ct.ListElementCell)

    val procPlan = parentPlan.forkPlan()

    // Calculate the length of our argument list
    val argLengthTemp = ps.Temp(vt.UInt32)
    procPlan.steps += ps.CalcProperListLength(argLengthTemp, argListHeadTemp)

    val resultTemp = planClauseTests(
      plannedClauses=plannedClauses,
      innerSelfTempOpt=innerSelfTempOpt,
      closureType=closureType,
      argListHeadTemp=argListHeadTemp,
      argLengthTemp=argLengthTemp
    )(procPlan, worldPtrTemp)

    procPlan.steps += ps.Return(Some(resultTemp))

    // Determine our signature
    val signature = ProcedureSignature(
      hasWorldArg=true,
      hasSelfArg=closureRequired,
      restArgMemberTypeOpt=Some(vt.AnySchemeType),
      fixedArgTypes=Nil,
      returnType=vt.ReturnType.ArbitraryValues,
      attributes=Set(ProcedureAttribute.FastCC)
    ) : ProcedureSignature
   
    // Store the planend procedures in our closure
    val outerSelfTempOpt = if (closureRequired) {
      // Save the closure values from the parent's scope
      val cellTemp = ps.RecordTemp()
      val dataTemp = ps.RecordLikeDataTemp()

      parentPlan.steps += ps.InitRecordLike(cellTemp, dataTemp, closureType, isUndefined=false)

      for(plannedClause <- plannedClauses;
          capturedProc <- plannedClause.capturedProcOpt) {
        val recordField = capturedProc.recordField
        val procTemp = capturedProc.outerProcTemp

        parentPlan.steps += ps.SetRecordDataField(dataTemp, closureType, recordField, procTemp)
      }
      
      // Store our entry point
      val entryPointTemp = ps.EntryPointTemp()
      parentPlan.steps += ps.CreateNamedEntryPoint(entryPointTemp, signature, nativeSymbol)
      parentPlan.steps += ps.SetProcedureEntryPoint(cellTemp, entryPointTemp)

      Some(cellTemp)
    }
    else {
      None
    }

    val plannedFunction = PlannedFunction(
      signature=signature,
      namedArguments=List(
        "world" -> worldPtrTemp
      ) ++
      innerSelfTempOpt.toList.map({ procSelf =>
        ("self" -> procSelf)
      }) ++
      List(
        "restArg" -> argListHeadTemp
      ),
      steps=procPlan.steps.toList,
      worldPtrOpt=Some(worldPtrTemp),
      debugContextOpt=None,
      irCommentOpt=None
    )

    parentPlan.plannedFunctions += (nativeSymbol -> plannedFunction)

    val knownClauses = plannedClauses map { case PlannedClause(procValue, capturedProcOpt) =>
      iv.KnownCaseLambdaClause(procValue, capturedProcOpt.map(_.recordField))
    }

    new iv.KnownCaseLambdaProc(
      signature=signature,
      closureType=closureType,
      clauses=knownClauses,
      plannedSymbol=nativeSymbol,
      selfTempOpt=outerSelfTempOpt,
      clausesInScope=true
    )
  }
}
