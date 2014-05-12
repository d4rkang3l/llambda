package io.llambda.compiler.analyzer
import io.llambda

import llambda.compiler.{StorageLocation, et}

private[analyzer] case class FoundVars(
  mutableVars : Set[StorageLocation] = Set(),
  initializers : Map[StorageLocation, et.Expression] = Map(),
  usedVars : Set[StorageLocation] = Set()
) {
  def ++(other : FoundVars) : FoundVars =
    FoundVars(
      mutableVars=mutableVars ++ other.mutableVars,
      initializers=initializers ++ other.initializers,
      usedVars=usedVars ++ other.usedVars
    )
}

private[analyzer] object FindVars {
  def apply(expr : et.Expression) : FoundVars = {
    // Find any mutable vars from our subexpressions
    val subexprFoundVars = expr.subexpressions.foldLeft(FoundVars()) { (previousFoundVars, expr) =>
      apply(expr) ++ previousFoundVars
    }

    expr match {
      case et.MutateVar(storageLoc, _) =>
        subexprFoundVars.copy(
          mutableVars=subexprFoundVars.mutableVars + storageLoc
        )

      case et.Bind(bindings) =>
        val bindingsToOpt = (bindings.map { case (storageLoc, initializer) =>
          storageLoc -> initializer
        }).toMap

        subexprFoundVars.copy(
          initializers=subexprFoundVars.initializers ++ bindingsToOpt
        )

      case et.VarRef(storageLoc) =>
        subexprFoundVars.copy(
          usedVars=subexprFoundVars.usedVars + storageLoc
        )

      case _ =>
        subexprFoundVars
    }
  }
}
