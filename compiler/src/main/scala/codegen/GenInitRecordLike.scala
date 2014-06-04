package io.llambda.compiler.codegen
import io.llambda

import llambda.llvmir._
import llambda.compiler.planner.{step => ps}

object GenInitRecordLike {
  case class InitializedRecordLike(recordCell : IrValue, recordData : IrValue)

  def apply(state : GenerationState, typeGenerator : TypeGenerator)(initStep : ps.InitRecordLike) : (GenerationState, InitializedRecordLike)  = { 
    val cellType = initStep.recordLikeType.cellType

    val block = state.currentBlock
    val module = block.function.module

    // Get a pointer to the new cell
    val allocation = state.currentAllocation
    val (newAllocation, recordCell) = allocation.consumeCells(block)(1, cellType) 
    
    // Get our record type information
    val recordLikeType = initStep.recordLikeType
    val generatedType = typeGenerator(recordLikeType)
    val recordDataIrType = generatedType.irType 
    
    // Set the class ID
    val classIdIr = IntegerConstant(cellType.recordClassIdIrType, generatedType.classId)
    cellType.genStoreToRecordClassId(block)(classIdIr, recordCell)

    val uncastRecordData = generatedType.storageType match {
      case TypeDataStorage.Empty =>
        cellType.genStoreToDataIsInline(block)(IntegerConstant(cellType.dataIsInlineIrType, 1), recordCell)

        // No fields; don't bother allocating or setting the recordData pointer
        NullPointerConstant(PointerType(IntegerType(8)))

      case TypeDataStorage.Inline =>
        cellType.genStoreToDataIsInline(block)(IntegerConstant(cellType.dataIsInlineIrType, 1), recordCell)

        // Store the value inline in the cell on top of the recordData field instead of going through another level of
        // indirection
        cellType.genPointerToRecordData(block)(recordCell)

      case TypeDataStorage.OutOfLine =>
        val recordDataAllocDecl = RuntimeFunctions.recordDataAlloc

        cellType.genStoreToDataIsInline(block)(IntegerConstant(cellType.dataIsInlineIrType, 0), recordCell)

        // Declare _lliby_record_data_alloc 
        module.unlessDeclared(recordDataAllocDecl) {
          module.declareFunction(recordDataAllocDecl)
        }
    
        // Find the size of the record data
        val irSize = GenSizeOf(block)(recordDataIrType)
    
        // Allocate it using _lliby_record_data_alloc
        val voidRecordData = block.callDecl(Some("rawRecordData"))(recordDataAllocDecl, List(irSize)).get

        // Store the record data pointer in the new cell
        cellType.genStoreToRecordData(block)(voidRecordData, recordCell)

        voidRecordData
    }

    val castRecordData = block.bitcastTo("castRecordData")(uncastRecordData, PointerType(recordDataIrType))

    val newState = state.copy(
      currentAllocation=newAllocation
    )

    val recordLike = InitializedRecordLike(
      recordCell=recordCell,
      recordData=castRecordData
    )

    (newState, recordLike)
  }
}