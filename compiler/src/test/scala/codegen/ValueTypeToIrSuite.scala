package io.llambda.compiler.codegen
import io.llambda

import llambda.llvmir
import llambda.compiler.{celltype => ct}
import llambda.compiler.{valuetype => vt}
import org.scalatest.FunSuite

class ValueTypeToIrSuite extends FunSuite {
  test("bool") {
    val typeWithSign = ValueTypeToIr(vt.Predicate)

    assert(typeWithSign.irType === llvmir.IntegerType(1))
    assert(typeWithSign.signed === Some(false))
  }
  
  test("int8") {
    val typeWithSign = ValueTypeToIr(vt.Int8)
    
    assert(typeWithSign.irType === llvmir.IntegerType(8))
    assert(typeWithSign.signed === Some(true))
  }
  
  test("int16") {
    val typeWithSign = ValueTypeToIr(vt.Int16)

    assert(typeWithSign.irType === llvmir.IntegerType(16))
    assert(typeWithSign.signed === Some(true))
  }
  
  test("int32") {
    val typeWithSign = ValueTypeToIr(vt.Int32)

    assert(typeWithSign.irType === llvmir.IntegerType(32))
    assert(typeWithSign.signed === Some(true))
  }
  
  test("int64") {
    val typeWithSign = ValueTypeToIr(vt.Int64)

    assert(typeWithSign.irType === llvmir.IntegerType(64))
    assert(typeWithSign.signed === Some(true))
  }

  test("uint8") {
    val typeWithSign = ValueTypeToIr(vt.UInt8)

    assert(typeWithSign.irType === llvmir.IntegerType(8))
    assert(typeWithSign.signed === Some(false))
  }
  
  test("uint16") {
    val typeWithSign = ValueTypeToIr(vt.UInt16)

    assert(typeWithSign.irType === llvmir.IntegerType(16))
    assert(typeWithSign.signed === Some(false))
  }
  
  test("uint32") {
    val typeWithSign = ValueTypeToIr(vt.UInt32)

    assert(typeWithSign.irType === llvmir.IntegerType(32))
    assert(typeWithSign.signed === Some(false))
  }
  
  test("float") {
    val typeWithSign = ValueTypeToIr(vt.Float)

    assert(typeWithSign.irType === llvmir.FloatType)
    assert(typeWithSign.signed === None)
  }
  
  test("double") {
    val typeWithSign = ValueTypeToIr(vt.Double)

    assert(typeWithSign.irType === llvmir.DoubleType)
    assert(typeWithSign.signed === None)
  }
  
  test("datum cell") {
    val typeWithSign = ValueTypeToIr(vt.AnySchemeType)

    assert(typeWithSign.irType === llvmir.PointerType(ct.AnyCell.irType))
    assert(typeWithSign.signed === None)
  }
  
  test("boolean cell") {
    val typeWithSign = ValueTypeToIr(vt.BooleanType)

    assert(typeWithSign.irType === llvmir.PointerType(ct.BooleanCell.irType))
    assert(typeWithSign.signed === None)
  }

  test("unicode char") {
    val typeWithSign = ValueTypeToIr(vt.UnicodeChar)

    assert(typeWithSign.irType === llvmir.IntegerType(32))
    assert(typeWithSign.signed === Some(true))
  }

  test("record cell") {
    val recordType = new vt.RecordType("recordType", Nil)

    val typeWithSign = ValueTypeToIr(recordType)

    assert(typeWithSign.irType === llvmir.PointerType(ct.RecordCell.irType))
    assert(typeWithSign.signed === None)
  }
}
