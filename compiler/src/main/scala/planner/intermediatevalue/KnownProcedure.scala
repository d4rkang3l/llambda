package io.llambda.compiler.planner.intermediatevalue
import io.llambda

import llambda.compiler.ProcedureSignature
import llambda.compiler.{celltype => ct}
import llambda.compiler.{valuetype => vt}
import llambda.compiler.planner.{step => ps}
import llambda.compiler.planner.{PlanWriter, InvokableProcedure, PlanProcedureTrampoline}
import llambda.compiler.codegen.AdaptedProcedureSignature
import llambda.compiler.InternalCompilerErrorException
import llambda.compiler.RuntimeErrorMessage

/** Represents a procedure with a known signature and direct entry point
  *
  * These procedures can be called directly without going through a costly trampoline. If this is converted to a
  * ct.ProcedureCell a trampoline will be dynamically built to give it an adapted signature. Adapted signatures are the
  * same for all procedures so they can be called without specific knowledge of the backing procedure. These adapted
  * procedure values are represented by InvokableProcedureCell
  *
  * @param signature     Signature of the procedure
  * @param nativeSymbol  Native symbol of the direct entry point to the procedure
  * @param selfTempOpt   For procedures with closures a procedure cell containing the procedure's closure. The entry
  *                      point does not have to be initialized; it will be set dynamically to a generated trampoline
  *                      if this value is explicitly converted to a ct.ProcedureCell
  * @param reportName    Name of this procedure in R7RS. This is used as a tag to implement certain optimizations
  *                      elsewhere in the planner. It is not directly used by this class
  */
class KnownProcedure(val signature : ProcedureSignature, nativeSymbol : String, selfTempOpt : Option[ps.TempValue], val reportName : Option[String] = None) extends IntermediateValue with InvokableProcedure with BoxedOnlyValue {
  val schemeType = vt.ProcedureType
  val typeDescription = "procedure"
  
  def toInvokableProcedure()(implicit plan : PlanWriter, worldPtr : ps.WorldPtrValue) : Option[InvokableProcedure] = 
    Some(this)

  def toSchemeTempValue(targetType : vt.SchemeType, errorMessageOpt : Option[RuntimeErrorMessage])(implicit plan : PlanWriter, worldPtr : ps.WorldPtrValue) : ps.TempValue = {
    if (schemeType.satisfiesType(targetType).get == true) {
      // Store an entry point with an adapted signature
      val entryPointTemp = if (signature == AdaptedProcedureSignature) {
        // The procedure already has the correct signature
        // This is unlikely but worth checking
        val entryPointTemp = ps.EntryPointTemp()
        plan.steps += ps.CreateNamedEntryPoint(entryPointTemp, signature, nativeSymbol)

        entryPointTemp
      }
      else {
        // Give the trampoline a sufficently scary looking symbol
        val trampolineSymbol = "__llambda_" + nativeSymbol + "_trampoline"

        // Ensure this already hasn't been planned
        if (!plan.plannedFunctions.contains(trampolineSymbol)) {
          // Plan the trampoline
          plan.plannedFunctions += (trampolineSymbol -> PlanProcedureTrampoline(signature, nativeSymbol))
        }

        // Load the trampoline's entry point
        val trampEntryPointTemp = ps.EntryPointTemp()
        plan.steps += ps.CreateNamedEntryPoint(trampEntryPointTemp, AdaptedProcedureSignature, trampolineSymbol) 

        trampEntryPointTemp
      }
      
      val cellTemp = selfTempOpt match {
        case Some(selfTemp) =>
          // Store the entry point in the procedure cell containing our closure
          // data
          plan.steps += ps.SetProcedureEntryPoint(selfTemp, entryPointTemp)

          selfTemp

        case None =>
          // This must have an empty closure
          // If we had a closure selfTempOpt would have been defined to contain it
          // This means we have to create a new closureless procedure cell to
          // contain the entry point
          val cellTemp = ps.CellTemp(ct.ProcedureCell)

          plan.steps += ps.CreateEmptyClosure(cellTemp, entryPointTemp)

          cellTemp
      }
    
      cellTempToSupertype(cellTemp, ct.ProcedureCell, targetType.cellType)
    }
    else {
      impossibleConversion(s"Cannot convert ${typeDescription} to non-procedure type ${targetType.schemeName}")
    }
  }
  
  def withReportName(newReportName : String) : KnownProcedure = {
    new KnownProcedure(signature, nativeSymbol, selfTempOpt, Some(newReportName))
  }
  
  def planEntryPoint()(implicit plan : PlanWriter) : ps.TempValue = {
    val entryPointTemp = ps.EntryPointTemp()
    plan.steps += ps.CreateNamedEntryPoint(entryPointTemp, signature, nativeSymbol)

    entryPointTemp
  }

  def planSelf()(implicit plan : PlanWriter) : ps.TempValue = selfTempOpt getOrElse {
    throw new InternalCompilerErrorException("Attempted to get self value of a procedure without a closure")
  }
  
  def preferredRepresentation : vt.ValueType =
    vt.ProcedureType

  def needsClosureRepresentation  = 
    // We only need a closure if we have a closure ourselves (i.e. a self temp)
    selfTempOpt.isDefined
  
  override def restoreFromClosure(valueType : vt.ValueType, varTemp : ps.TempValue) : IntermediateValue = {
    new KnownProcedure(signature, nativeSymbol, Some(varTemp), reportName)
  }
}

