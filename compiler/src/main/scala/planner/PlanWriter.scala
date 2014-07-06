package io.llambda.compiler.planner
import io.llambda

import collection.mutable

import llambda.compiler.planner.{step => ps}
import llambda.compiler.{ContextLocated, NoContextLocation}
import llambda.compiler.InternalCompilerErrorException
import llambda.compiler.et
import llambda.compiler.{valuetype => vt}
import llambda.compiler.planner.{intermediatevalue => iv}

class PlanWriter(
    val plannedFunctions : mutable.Map[String, PlannedFunction],
    val allocedProcSymbols : mutable.HashSet[String],
    val plannedTypePredicates : mutable.Map[vt.SchemeType, iv.KnownTypePredicateProc]
) {
  private val contextLocStack = new mutable.Stack[ContextLocated] 

  class StepBuilder {
    private val stepBuffer = new mutable.ListBuffer[ps.Step] 

    def +=(step : ps.Step) {
      for(contextLoc <- contextLocStack.headOption) {
        // Context locate this step
        step.assignLocationFrom(contextLoc)
      }

      stepBuffer += step
    }

    def toList : List[ps.Step] = 
      stepBuffer.toList
  }

  val steps = new StepBuilder

  /** Pushes a new source location on to the location stack for the duration of block
    *
    * This will implicitly locate any plan steps added while the block is being executed
    */
  def withContextLocation[T](contextLocated : ContextLocated)(block : => T) : T = {
    contextLocStack.push(contextLocated)

    try {
      block
    }
    finally {
      contextLocStack.pop
    }
  }

  /** Returns the active source location or NoContextLocation if the location is not available */
  def activeContextLocated =
    contextLocStack.headOption.getOrElse(NoContextLocation)

  def withContextLocationOpt[T](contextLocatedOpt : Option[ContextLocated])(block : => T) : T = {
    contextLocatedOpt match {
      case Some(contextLocated) =>
        withContextLocation(contextLocated)(block)

      case _ =>
        block
    }
  }

  private def findNextFreeSymbol(sourceName : String) : String = {
    val allKnownSymbols = plannedFunctions.keySet ++ allocedProcSymbols

    if (!allKnownSymbols.contains(sourceName)) {
      sourceName
    }
    else {
      for(suffix <- Stream.from(1)) {
        val suffixedName = sourceName + "_" + suffix.toString

        if (!allKnownSymbols.contains(suffixedName)) {
          return suffixedName
        }
      }
      
      throw new InternalCompilerErrorException("Ran out of natural numbers")
    }
  }

  /** Allocates a unique name for a procedure
    *
    * codegen expects a flat namespace for procedures but the same name can appear in multiple Scheme scopes 
    */
  def allocProcedureSymbol(sourceName : String) : String = {
    val freeSymbol = findNextFreeSymbol(sourceName)
    allocedProcSymbols += freeSymbol

    freeSymbol
  }

  def forkPlan() : PlanWriter = 
    // All forks share plannedFunctions
    new PlanWriter(plannedFunctions, allocedProcSymbols, plannedTypePredicates)

  def buildCondBranch(test : ps.TempValue, trueBuilder : (PlanWriter) => ps.TempValue, falseBuilder : (PlanWriter) => ps.TempValue) : ps.TempValue = {
    val truePlan = forkPlan()
    val trueValue = trueBuilder(truePlan) 

    val falsePlan = forkPlan()
    val falseValue = falseBuilder(falsePlan)

    if (trueValue.isGcManaged != falseValue.isGcManaged) {
      throw new InternalCompilerErrorException("phi branches returning GC incompatible values")
    }

    val phiTemp = new ps.TempValue(trueValue.isGcManaged)

    this.steps += ps.CondBranch(phiTemp, test, truePlan.steps.toList, trueValue, falsePlan.steps.toList, falseValue)

    // Return the phi'ed value
    phiTemp
  }
}

object PlanWriter {
  def apply() =
    new PlanWriter(new mutable.HashMap[String, PlannedFunction], new mutable.HashSet[String], new mutable.HashMap[vt.SchemeType, iv.KnownTypePredicateProc])
}
