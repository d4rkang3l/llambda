package io.llambda.compiler.planner
import io.llambda

import llambda.compiler.et
import llambda.compiler.{valuetype => vt}
import llambda.compiler.planner.{intermediatevalue => iv}
import llambda.compiler.StorageLocation
import llambda.compiler.codegen.CompactRepresentationForType
  
private sealed abstract class ClosedVariable

private sealed trait ClosedImmutable extends ClosedVariable {
  val storageLoc : StorageLocation
  val parentIntermediate : iv.IntermediateValue
}

private case class ImportedImmutable(
  storageLoc : StorageLocation,
  parentIntermediate : iv.IntermediateValue
) extends ClosedImmutable

private sealed abstract class CapturedVariable extends ClosedVariable {
  val storageLoc : StorageLocation
  val valueType : vt.ValueType
  val recordField : vt.RecordField
}

private case class CapturedImmutable(
  storageLoc : StorageLocation,
  parentIntermediate : iv.IntermediateValue,
  valueType : vt.ValueType,
  recordField : vt.RecordField
) extends CapturedVariable with ClosedImmutable

private case class CapturedMutable(
  storageLoc : StorageLocation,
  parentMutable : MutableValue,
  recordField : vt.RecordField
) extends CapturedVariable {
  val valueType = parentMutable.mutableType
}
 
private[planner] object FindClosedVars {
  /** Finds all referenced variables in an expression and returns them in a stable order */
  private def findRefedVariables(expr : et.Expr) : List[StorageLocation] = expr match {
    case et.VarRef(variable) =>
      List(variable)

    case et.MutateVar(variable, expr) =>
      variable :: findRefedVariables(expr)

    case otherExpr =>
      otherExpr.subexprs.flatMap(findRefedVariables)
  }

  def apply(parentState : PlannerState, bodyExpr : et.Expr) : List[ClosedVariable] = {
    // Find the variables that are closed by the parent scope
    val refedVarsList = findRefedVariables(bodyExpr)

    // Figure out if the immutables need to be captured
    refedVarsList.distinct flatMap { storageLoc =>
      parentState.values.get(storageLoc) map {
        case ImmutableValue(parentIntermediate) =>
          if (parentIntermediate.needsClosureRepresentation) {
            val compactType = CompactRepresentationForType(parentIntermediate.preferredRepresentation)
            val recordField = new vt.RecordField(storageLoc.sourceName, compactType)

            // We have to capture this
            CapturedImmutable(storageLoc, parentIntermediate, compactType, recordField)
          }
          else {
            // No need for capturing - import the intermediate value directly
            ImportedImmutable(storageLoc, parentIntermediate)
          }

      case parentMutable : MutableValue =>
        val recordField = new vt.RecordField(storageLoc.sourceName, parentMutable.mutableType)
        CapturedMutable(storageLoc, parentMutable, recordField)
      }
    }
  }
}