package io.llambda.compiler.planner.intermediatevalue
import io.llambda

import llambda.compiler.{ProcedureSignature, ContextLocated}
import llambda.compiler.{valuetype => vt}
import llambda.compiler.planner.{step => ps}
import llambda.compiler.planner._

/** Represents a user-provided procedure with a known signature and direct entry point
  *
  * @param signature     Signature of the procedure
  * @param nativeSymbol  Native symbol of the direct entry point to the procedure
  * @param selfTempOpt   For procedures with closures a procedure cell containing the procedure's closure. The entry
  *                      point does not have to be initialized; it will be set dynamically to a generated trampoline
  *                      if this value is explicitly converted to a ct.ProcedureCell
  * @param reportName    Name of this procedure in R7RS. This is used as a tag to implement certain optimizations
  *                      elsewhere in the planner. It is not directly used by this class
  */
final class KnownUserProc(signature : ProcedureSignature, nativeSymbol : String, selfTempOpt : Option[ps.TempValue], val reportNameOpt : Option[String] = None) extends KnownProc(signature, nativeSymbol, selfTempOpt) {
  // These objects know how to implement certain report procedure directly
  // with plan steps
  private val reportProcPlanners = List[reportproc.ReportProcPlanner](
    reportproc.ApplyProcPlanner,
    reportproc.BooleanProcPlanner,
    reportproc.CadrProcPlanner,
    reportproc.EquivalenceProcPlanner,
    reportproc.ListProcPlanner,
    reportproc.NumberProcPlanner,
    reportproc.VectorProcPlanner
  )

  def withReportName(newReportName : String) : KnownUserProc = {
    new KnownUserProc(signature, nativeSymbol, selfTempOpt, Some(newReportName))
  }
  
  override def restoreFromClosure(valueType : vt.ValueType, varTemp : ps.TempValue) : IntermediateValue = {
    new KnownUserProc(signature, nativeSymbol, Some(varTemp), reportNameOpt)
  }
  
  override def attemptInlineApplication(state : PlannerState)(operands : List[(ContextLocated, IntermediateValue)])(implicit plan : PlanWriter, worldPtr : ps.WorldPtrValue) : Option[PlanResult] = {
    // Find the first report proc planner that knowns how to plan us
    for(reportName <- reportNameOpt;
        reportProcPlanner <- reportProcPlanners;
        planResult <- reportProcPlanner(state)(reportName, operands)) {
      return Some(planResult)
    }

    None
  }
}
