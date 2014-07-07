package io.llambda.compiler.planner
import io.llambda

import llambda.compiler.et
import llambda.compiler.ProcedureSignature
import llambda.compiler.{valuetype => vt}
import llambda.compiler.planner.{step => ps}

object PlanRecordMutator {
  def apply(expr : et.RecordMutator)(implicit parentPlan : PlanWriter) : PlannedFunction = 
    expr match {
      case et.RecordMutator(recordType, field) =>
        // Determine our signature
        val mutatorSignature = ProcedureSignature(
          hasWorldArg=false,
          hasSelfArg=false,
          hasRestArg=false,
          fixedArgs=List(recordType, field.fieldType),
          returnType=None,
          attributes=Set()
        )

        // Set up our arguments
        val recordCellTemp = ps.RecordTemp()
        val newValueTemp = ps.Temp(field.fieldType)

        val namedArguments = List(
          ("recordCell" -> recordCellTemp),
          ("newValue" -> newValueTemp)
        )
        
        val plan = parentPlan.forkPlan()
        
        // Extract the record data
        val recordDataTemp = ps.RecordLikeDataTemp()
        plan.steps += ps.LoadRecordLikeData(recordDataTemp, recordCellTemp, recordType) 

        // Store the new value
        plan.steps += ps.SetRecordDataField(recordDataTemp, recordType, field, newValueTemp) 
        plan.steps += ps.Return(None)

        PlannedFunction(
          signature=mutatorSignature,
          namedArguments=namedArguments,
          steps=plan.steps.toList,
          worldPtrOpt=None,
          debugContextOpt=None
        )
    }
}

