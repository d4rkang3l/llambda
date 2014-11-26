package io.llambda.compiler.planner.intermediatevalue
import io.llambda

import llambda.compiler.{celltype => ct}
import llambda.compiler.{valuetype => vt}
import llambda.compiler.valuetype.Implicits._
import llambda.compiler.planner.{step => ps}
import llambda.compiler.planner.{PlanWriter, BoxedValue}
import llambda.compiler.RuntimeErrorMessage

sealed abstract class ConstantValue(val cellType : ct.ConcreteCellType) extends IntermediateValue with UninvokableValue {
  val schemeType : vt.SchemeType = vt.SchemeTypeAtom(cellType)
    
  def preferredRepresentation : vt.ValueType =
    schemeType
  
  def needsClosureRepresentation : Boolean =
    false
}

sealed abstract class TrivialConstantValue[T, U <: ps.CreateConstantCell](cellType : ct.ConcreteCellType, value : T, stepConstructor : (ps.TempValue, T) => U) extends ConstantValue(cellType) {
  def toBoxedValue()(implicit plan : PlanWriter) : BoxedValue = {
    val constantTemp = ps.CellTemp(cellType, knownConstant=true)
    plan.steps += stepConstructor(constantTemp, value)

    BoxedValue(cellType, constantTemp)
  }
}

class ConstantStringValue(val value : String) extends TrivialConstantValue(ct.StringCell, value, ps.CreateStringCell.apply) with BoxedOnlyValue {
  val typeDescription = "constant string"
}

class ConstantSymbolValue(val value : String) extends TrivialConstantValue(ct.SymbolCell, value, ps.CreateSymbolCell.apply) with BoxedOnlyValue {
  val typeDescription = "constant symbol"

  override val schemeType = vt.LiteralSymbolType(value)
}

trait ConstantNumberValue extends ConstantValue with UnboxedValue {
  def doubleValue : Double
}

class ConstantExactIntegerValue(val value : Long) extends TrivialConstantValue(ct.ExactIntegerCell, value, ps.CreateExactIntegerCell.apply) with ConstantNumberValue {
  val typeDescription = "constant exact integer"
  val nativeType = vt.Int64

  def toNativeTempValue(nativeType : vt.NativeType, errorMessageOpt : Option[RuntimeErrorMessage])(implicit plan : PlanWriter) : ps.TempValue = nativeType match {
    case intType : vt.IntType =>
      val minIntValue = if (intType.signed) {
        -1L << (intType.bits - 1)
      }
      else {
        0
      }
      
      val maxIntValue = if (intType.signed) {
        (1L << (intType.bits - 1)) - 1
      }
      else {
        (1L << intType.bits) - 1
      }

      if ((value < minIntValue) || (value > maxIntValue)) {
        val message = s"Constant exact integer ${value} cannot be represented by native integer type ${nativeType}"
        impossibleConversion(message)
      }

      val constantTemp = ps.Temp(intType)
      plan.steps += ps.CreateNativeInteger(constantTemp, value, intType.bits)
      constantTemp

    case fpType : vt.FpType =>
      impossibleConversion(s"Cannot convert ${typeDescription} to floating point native type ${vt.NameForType(nativeType)}. Consider using (inexact) to explicitly convert the value.")

    case _ =>
      impossibleConversion(s"Cannot convert ${typeDescription} to non-integer native type ${vt.NameForType(nativeType)}")
  }

  def doubleValue : Double =
    value.toDouble
}

class ConstantFlonumValue(val value : Double) extends TrivialConstantValue(ct.FlonumCell, value, ps.CreateFlonumCell.apply) with ConstantNumberValue {
  val typeDescription = "constant flonum"
  val nativeType = vt.Double

  def toNativeTempValue(nativeType : vt.NativeType, errorMessageOpt : Option[RuntimeErrorMessage])(implicit plan : PlanWriter) : ps.TempValue = nativeType match {
    case fpType : vt.FpType =>
      val constantTemp = ps.Temp(fpType)
      plan.steps += ps.CreateNativeFloat(constantTemp, value, fpType)
      constantTemp
    
    case intType : vt.IntType =>
      impossibleConversion(s"Cannot convert ${typeDescription} to integer native type ${vt.NameForType(nativeType)}. Consider using (exact) to explicitly convert the value.")

    case _ => 
      impossibleConversion(s"Cannot convert ${typeDescription} to non-floating point native type ${vt.NameForType(nativeType)}")
  }

  def doubleValue : Double =
    value
}

class ConstantCharValue(val value : Int) extends TrivialConstantValue(ct.CharCell, value, ps.CreateCharCell.apply) with UnboxedValue {
  val typeDescription = "constant character"
  val nativeType = vt.UnicodeChar

  def toNativeTempValue(nativeType : vt.NativeType, errorMessageOpt : Option[RuntimeErrorMessage])(implicit plan : PlanWriter) : ps.TempValue = nativeType match {
    case vt.UnicodeChar =>
      val constantTemp = ps.Temp(vt.UnicodeChar)
      plan.steps += ps.CreateNativeInteger(constantTemp, value, vt.UnicodeChar.bits)
      constantTemp

    case _ =>
      impossibleConversion(s"Cannot convert ${typeDescription} to non-character native type ${vt.NameForType(nativeType)}")
  }
}

class ConstantBooleanValue(val value : Boolean) extends TrivialConstantValue(ct.BooleanCell, value, ps.CreateBooleanCell.apply) with UnboxedValue {
  override val schemeType = vt.LiteralBooleanType(value)
  val typeDescription = vt.NameForType(schemeType)
  val nativeType = vt.Predicate

  private val intValue = if (value) 1 else 0

  override def toTruthyPredicate()(implicit plan : PlanWriter) : ps.TempValue = {
    val predTemp = ps.Temp(vt.Predicate)
    plan.steps += ps.CreateNativeInteger(predTemp, intValue, vt.Predicate.bits) 

    predTemp
  }

  def toNativeTempValue(nativeType : vt.NativeType, errorMessageOpt : Option[RuntimeErrorMessage])(implicit plan : PlanWriter) : ps.TempValue = 
    // toTruthyPredicate() will catch our conversion to bool
    impossibleConversion(s"Cannot convert ${typeDescription} to non-boolean native type ${vt.NameForType(nativeType)}")
}

class ConstantBytevectorValue(val elements : Vector[Short]) extends TrivialConstantValue(ct.BytevectorCell, elements, ps.CreateBytevectorCell.apply) with BoxedOnlyValue {
  val typeDescription = "constant bytevector"
}

class ConstantPairValue(val car : ConstantValue, val cdr : ConstantValue) extends ConstantValue(ct.PairCell) with BoxedOnlyValue with KnownPair {
  override val schemeType : vt.SchemeType = vt.PairType(car.schemeType, cdr.schemeType)
  val typeDescription = "constant pair"

  def toBoxedValue()(implicit plan : PlanWriter) : BoxedValue = {
    val constantTemp = ps.CellTemp(cellType, knownConstant=true)

    // Box our car/cdr first
    val carTemp = car.toTempValue(vt.AnySchemeType)
    val cdrTemp = cdr.toTempValue(vt.AnySchemeType)

    plan.steps += ps.CreatePairCell(constantTemp, carTemp, cdrTemp, listLengthOpt)

    BoxedValue(cellType, constantTemp)
  }
}

class ConstantVectorValue(val elements : Vector[ConstantValue]) extends ConstantValue(ct.VectorCell) with BoxedOnlyValue {
  override val schemeType : vt.SchemeType = vt.SpecificVectorType(elements.map { element =>
    vt.DirectSchemeTypeRef(element.schemeType)
  })

  val typeDescription = "constant vector"

  def toBoxedValue()(implicit plan : PlanWriter) : BoxedValue = {
    val constantTemp = ps.CellTemp(cellType, knownConstant=true)

    // Box our elements
    val elementTemps = elements.map {
      _.toTempValue(vt.AnySchemeType)
    }

    plan.steps += ps.CreateVectorCell(constantTemp, elementTemps)

    BoxedValue(cellType, constantTemp)
  }
}

object EmptyListValue extends ConstantValue(ct.EmptyListCell) with BoxedOnlyValue with KnownListElement {
  val typeDescription = "constant empty list"

  def toBoxedValue()(implicit plan : PlanWriter) : BoxedValue = {
    val constantTemp = ps.CellTemp(cellType, knownConstant=true)
    plan.steps += ps.CreateEmptyListCell(constantTemp)
    BoxedValue(cellType, constantTemp)
  }
  
  // KnownListElement implementation
  lazy val listLengthOpt = Some(0L)
  def toValueListOpt = Some(Nil)
}

object UnitValue extends ConstantValue(ct.UnitCell) with BoxedOnlyValue {
  val typeDescription = "constant unit value"

  def toBoxedValue()(implicit plan : PlanWriter) : BoxedValue = {
    val constantTemp = ps.CellTemp(cellType, knownConstant=true)
    plan.steps += ps.CreateUnitCell(constantTemp)
    BoxedValue(cellType, constantTemp)
  }
}

