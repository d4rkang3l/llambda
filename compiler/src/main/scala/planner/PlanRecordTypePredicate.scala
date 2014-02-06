package io.llambda.compiler.planner
import io.llambda

import llambda.compiler.et
import llambda.compiler.ProcedureSignature
import llambda.compiler.{valuetype => vt}
import llambda.compiler.{celltype => ct}
import llambda.compiler.planner.{step => ps}
import llambda.compiler.planner.{intermediatevalue => iv}

object PlanRecordTypePredicate {
  def apply(expr : et.RecordTypePredicate)(implicit parentPlan : PlanWriter) : PlannedFunction = 
    expr match {
      case et.RecordTypePredicate(recordType) =>
        // Determine our signature
        val predicateSignature = ProcedureSignature(
          hasWorldArg=false,
          hasSelfArg=false,
          hasRestArg=false,
          // We must be able to take any data type without erroring out
          fixedArgs=List(vt.IntrinsicCellType(ct.DatumCell)),
          returnType=Some(vt.CBool)
        )
        
        // We only have a single argument
        val argumentTemp = ps.GcManagedValue()
        
        val plan = parentPlan.forkPlan()

        // Try to cast to a generic record type first
        val isRecordPred = ps.GcUnmanagedValue()
        plan.steps += ps.TestCellType(isRecordPred, argumentTemp, ct.RecordCell) 

        val retValueTemp = plan.buildCondBranch(isRecordPred, 
          {isRecordPlan =>
            // Cast the boxed type to a boxed record
            val recordCellTemp = ps.GcManagedValue()
            isRecordPlan.steps += ps.CastCellToTypeUnchecked(recordCellTemp, argumentTemp, ct.RecordCell)

            val classMatchedPred = ps.GcUnmanagedValue()
            isRecordPlan.steps += ps.TestRecordLikeClass(classMatchedPred, recordCellTemp, recordType) 

            val classMatchedBool = ps.GcUnmanagedValue()
            isRecordPlan.steps += ps.ConvertNativeInteger(classMatchedBool, classMatchedPred, vt.CBool.bits, false)

            classMatchedBool
          },
          {isNotRecordPlan =>
            val falseBool = ps.GcUnmanagedValue()
            isNotRecordPlan.steps += ps.StoreNativeInteger(falseBool, 0, vt.CBool.bits)

            falseBool
          })

        // Return the phi'ed value
        plan.steps += ps.Return(Some(retValueTemp))

        PlannedFunction(
          signature=predicateSignature,
          namedArguments=List(("value" -> argumentTemp)),
          steps=plan.steps.toList
        )
    }
}
