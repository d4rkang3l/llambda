package llambda.codegen.llvmir

sealed abstract class IrValue extends Irable {
  def irType : FirstClassType

  def toIrWithType = irType.toIr + " " + toIr
}

sealed abstract class IrConstant extends IrValue

sealed abstract class BoolConstant extends IrConstant {
  def irType = IntegerType(1)
}

sealed abstract class FloatingPointConstant extends IrConstant {
  def IrType : FloatingPointType
}

case object TrueConstant extends BoolConstant {
  def toIr = "true"
}

case object FalseConstant extends BoolConstant {
  def toIr = "false"
}

case class IntegerConstant(irType : IntegerType, value : Integer) extends IrConstant {
  def toIr = value.toString
}

case class SingleConstant(value : Float) extends IrConstant {
  def irType = SingleType

  def toIr = {
    if (value.isInfinity || value.isNaN) {
      "0x" + java.lang.Float.floatToIntBits(value).toHexString
    }
    else {
      value.toString
    }
  }
}

case class DoubleConstant(value : Double) extends IrConstant {
  def irType = DoubleType

  def toIr = {
    if (value.isInfinity || value.isNaN) {
      "0x" + java.lang.Double.doubleToLongBits(value).toHexString
    }
    else {
      value.toString
    }
  }
}

case class NullPointerConstant(irType : PointerType) extends IrConstant {
  def toIr = "null"
}

case class StructureConstant(members : List[IrConstant]) extends IrConstant {
  def irType = StructureType(members.map(_.irType))

  def toIr = "{" + members.map(_.toIrWithType).mkString(", ") + "}"
}

case class ArrayConstant(innerType : FirstClassType , members : List[IrConstant]) extends IrConstant {
  def irType = ArrayType(members.length, innerType)

  def toIr = "[" + members.map(_.toIrWithType).mkString(", ") + "]"
}
