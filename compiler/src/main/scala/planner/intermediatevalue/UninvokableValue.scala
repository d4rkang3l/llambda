package io.llambda.compiler.planner.intermediatevalue
import io.llambda

import llambda.compiler.planner.{PlanWriter, InvokableProcedure}

/** Trait for IntermediateValues that cannot be invokable */
trait UninvokableValue extends IntermediateValue {
  def toInvokableProcedure()(implicit plan : PlanWriter) : Option[InvokableProcedure] = None
}
