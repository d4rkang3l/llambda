package io.llambda.compiler.codegen
import io.llambda

import llambda.compiler.InternalCompilerErrorException
import llambda.compiler.ProcedureAttribute
import llambda.compiler.planner.{step => ps}
import llambda.llvmir._
import llambda.compiler.{celltype => ct}


object GenPlanStep {
  private val fastMathFlags = Set[FastMathFlag]()

  private def stepCompareCondToIntegerIr(stepCond: ps.CompareCond): IComparisonCond = stepCond match {
    case ps.CompareCond.Equal => IComparisonCond.Equal
    case ps.CompareCond.NotEqual => IComparisonCond.NotEqual
    case ps.CompareCond.GreaterThan => IComparisonCond.GreaterThan
    case ps.CompareCond.GreaterThanEqual => IComparisonCond.GreaterThanEqual
    case ps.CompareCond.LessThan => IComparisonCond.LessThan
    case ps.CompareCond.LessThanEqual => IComparisonCond.LessThanEqual
  }

  private def stepCompareCondToFloatIr(stepCond: ps.CompareCond): FComparisonCond = stepCond match {
    case ps.CompareCond.Equal => FComparisonCond.OrderedEqual
    case ps.CompareCond.NotEqual => FComparisonCond.OrderedNotEqual
    case ps.CompareCond.GreaterThan => FComparisonCond.OrderedGreaterThan
    case ps.CompareCond.GreaterThanEqual => FComparisonCond.OrderedGreaterThanEqual
    case ps.CompareCond.LessThan => FComparisonCond.OrderedLessThan
    case ps.CompareCond.LessThanEqual => FComparisonCond.OrderedLessThanEqual
  }

  def apply(state: GenerationState, genGlobals: GenGlobals)(step: ps.Step): GenResult = step match {
    case ps.AllocateHeapCells(count) =>
      if (!state.currentAllocation.isEmpty) {
        // This is not only wasteful but dangerous as the previous allocation won't be fully initialized
        throw new InternalCompilerErrorException("Attempted cell allocation without fully consuming previous allocation")
      }

      val worldPtrIr = state.liveTemps(ps.WorldPtrValue)

      val (allocState, allocation) = GenHeapAllocation.genAllocation(state)(worldPtrIr, count)
      allocState.copy(currentAllocation=allocation)

    case createConstantStep: ps.CreateConstant =>
      val irResult = genGlobals.constantGenerator(state, genGlobals)(createConstantStep)

      state.withTempValue(createConstantStep.result -> irResult)

    case unboxValueStep: ps.UnboxValue =>
      val irBoxed = state.liveTemps(unboxValueStep.boxed)
      val irResult = GenUnboxing(state)(unboxValueStep, irBoxed)

      state.withTempValue(unboxValueStep.result -> irResult)

    case boxValueStep: ps.BoxValue =>
      val irUnboxed = state.liveTemps(boxValueStep.unboxed)
      val (boxState, irResult) = GenBoxing(state)(boxValueStep, irUnboxed)

      boxState.withTempValue(boxValueStep.result -> irResult)

    case ps.CreateNamedEntryPoint(resultTemp, signature, nativeSymbol) =>
      val irModule = state.currentBlock.function.module
      val irValue = GenNamedEntryPoint(irModule)(signature, nativeSymbol, genGlobals.plannedSymbols)

      state.withTempValue(resultTemp -> irValue)

    case ps.CastCellToTypeUnchecked(resultTemp, subvalueTemp, targetType) =>
      val subvalueIr = state.liveTemps(subvalueTemp)

      val irValue = subvalueIr match {
        case constant: IrConstant =>
          // We can use an anonymous constant bitcast here
          BitcastToConstant(constant, PointerType(targetType.irType))

        case _ =>
          targetType.genPointerBitcast(state.currentBlock)(subvalueIr)
      }

      state.withTempValue(resultTemp -> irValue)

    case ps.ConvertNativeInteger(resultTemp, fromValueTemp, toBits, signed) =>
      val fromValueIr = state.liveTemps(fromValueTemp)
      val convertedIr = GenIntegerConversion(state.currentBlock)(fromValueIr, toBits, signed)

      state.withTempValue(resultTemp -> convertedIr)

    case ps.ConvertNativeIntegerToFloat(resultTemp, fromValueTemp, fromSigned, fpType) =>
      val fromValueIr = state.liveTemps(fromValueTemp)
      val resultIr = GenIntegerToFloatConversion(state.currentBlock)(fromValueIr, fromSigned, fpType)

      state.withTempValue(resultTemp -> resultIr)

    case ps.ConvertNativeFloat(resultTemp, fromValueTemp, fpType) =>
      val fromValueIr = state.liveTemps(fromValueTemp)
      val resultIr = GenFloatConversion(state.currentBlock)(fromValueIr, fpType)

      state.withTempValue(resultTemp -> resultIr)

    case ps.CalcProperListLength(resultTemp, listHeadTemp) =>
      val listHeadIr = state.liveTemps(listHeadTemp)
      val (listState, lengthIr) = GenCalcProperListLength(state)(listHeadIr)

      listState.withTempValue(resultTemp -> lengthIr)

    case ps.TestCellType(resultTemp, cellTemp, cellType, possibleTypes) =>
      val cellIr = state.liveTemps(cellTemp)

      // Build range metadata from our possible types
      val rangeMetadataOpt = RangeMetadata.fromPossibleValues(
        integerType=ct.AnyCell.typeIdIrType,
        possibleTypes.map(_.typeId)
      )

      val loadMetadata = rangeMetadataOpt match {
        case Some(rangeMetadata) => Map("range" -> rangeMetadata)
        case _ =>                   Map[String, Metadata]()
      }

      // Load the type ID
      val block = state.currentBlock
      val datumIr = ct.AnyCell.genPointerBitcast(block)(cellIr)
      val typeIdIr = ct.AnyCell.genLoadFromTypeId(block)(datumIr, metadata=loadMetadata)

      val resultIr = block.icmp(cellType.llvmName + "Check")(IComparisonCond.Equal, None, typeIdIr, IntegerConstant(ct.AnyCell.typeIdIrType, cellType.typeId))

      state.withTempValue(resultTemp -> resultIr)

    case condBranch: ps.CondBranch =>
      GenCondBranch(state, genGlobals)(condBranch)

    case invokeStep @ ps.Invoke(resultOpt, signature, funcPtrTemp, arguments, _) =>
      val result = ProcedureSignatureToIr(signature)
      val irSignature = result.irSignature
      val metadata = result.callMetadata

      val irFuncPtr = state.liveTemps(funcPtrTemp)
      val irArguments = arguments.map(state.liveTemps)

      val block = state.currentBlock

      if (signature.attributes.contains(ProcedureAttribute.NoReturn)) {
        // This can't return - terminate the function
        block.call(None)(irSignature, irFuncPtr, irArguments)
        block.unreachable
        return BlockTerminated
      }

      // This call can't allocate or throw exceptions - skip the barrier and invoke
      val irRetOpt = block.call(Some("ret"))(irSignature, irFuncPtr, irArguments, metadata=metadata)

      resultOpt match {
        case Some(resultTemp) =>
          state.withTempValue(resultTemp -> irRetOpt.get)

        case None =>
          state
      }

    case ps.TailCall(signature, funcPtrTemp, arguments) =>
      val result = ProcedureSignatureToIr(signature)
      val irSignature = result.irSignature
      val metadata = result.callMetadata

      val irFuncPtr = state.liveTemps(funcPtrTemp)
      val irArguments = arguments.map(state.liveTemps)

      if (irSignature.result.irType == VoidType) {
        state.currentBlock.call(None)(irSignature, irFuncPtr, irArguments, tailCall=true)
        state.currentBlock.retVoid()
      }
      else {
        val block = state.currentBlock
        val irRetValueOpt = block.call(Some("ret"))(
          irSignature,
          irFuncPtr,
          irArguments,
          tailCall=true,
          metadata=metadata
        )

        block.ret(irRetValueOpt.get)
      }

      BlockTerminated

    case ps.Return(None) =>
      state.currentBlock.retVoid()

      BlockTerminated

    case ps.Return(Some(returnValueTemp)) =>
      val irRetValue = state.liveTemps(returnValueTemp)
      state.currentBlock.ret(irRetValue)

      BlockTerminated

    case ps.LoadPairCar(resultTemp, pairTemp) =>
      val pairIr = state.liveTemps(pairTemp)
      val carIr = ct.PairCell.genLoadFromCar(state.currentBlock)(pairIr)

      state.withTempValue(resultTemp -> carIr)

    case ps.LoadPairCdr(resultTemp, pairTemp) =>
      val pairIr = state.liveTemps(pairTemp)
      val cdrIr = ct.PairCell.genLoadFromCdr(state.currentBlock)(pairIr)

      state.withTempValue(resultTemp -> cdrIr)

    case ps.LoadSymbolByteLength(resultTemp, symbolTemp, possibleLengthsOpt) =>
      val symbolIr = state.liveTemps(symbolTemp)
      val (newState, resultIr) = GenLoadSymbolByteLength(state)(symbolIr, possibleLengthsOpt)

      newState.withTempValue(resultTemp -> resultIr)

    case ps.LoadSymbolByte(resultTemp, symbolTemp, offsetTemp, symbolByteLength, possibleValuesOpt) =>
      val symbolIr = state.liveTemps(symbolTemp)
      val offsetIr = state.liveTemps(offsetTemp)

      val byteIr = GenLoadSymbolByte(state.currentBlock)(symbolIr, offsetIr, symbolByteLength, possibleValuesOpt)

      state.withTempValue(resultTemp -> byteIr)

    case ps.LoadBytevectorLength(resultTemp, bytevectorTemp) =>
      val rangeMetadata = RangeMetadata(
        ct.BytevectorCell.lengthIrType,
        (
          ct.BytevectorCellConstants.minPossibleLength,
          ct.BytevectorCellConstants.maxPossibleLength(genGlobals.targetPlatform) + 1
        )
      )

      val block = state.currentBlock
      val bytevectorIr = state.liveTemps(bytevectorTemp)
      val lengthIr = ct.BytevectorCell.genLoadFromLength(block)(bytevectorIr, Map("range" -> rangeMetadata))

      state.withTempValue(resultTemp -> lengthIr)

    case ps.InitVector(resultTemp, elements) =>
      val worldPtrIr = state.liveTemps(ps.WorldPtrValue)

      val vectorIr = GenVector.init(state)(worldPtrIr, elements)
      state.withTempValue(resultTemp -> vectorIr)

    case ps.InitFilledVector(resultTemp, length, fill) =>
      val worldPtrIr = state.liveTemps(ps.WorldPtrValue)
      val lengthIr = state.liveTemps(length)

      val (finalState, vectorIr) = GenVector.initFilled(state)(worldPtrIr, lengthIr, fill)

      finalState.withTempValue(resultTemp -> vectorIr)

    case ps.LoadVectorLength(resultTemp, vectorTemp) =>
      val rangeMetadata = RangeMetadata(
        ct.VectorCell.lengthIrType,
        (
          ct.VectorCellConstants.minPossibleLength,
          ct.VectorCellConstants.maxPossibleLength(genGlobals.targetPlatform) + 1
        )
      )

      val block = state.currentBlock
      val vectorIr = state.liveTemps(vectorTemp)
      val lengthIr = ct.VectorCell.genLoadFromLength(block)(vectorIr, Map("range" ->  rangeMetadata))

      state.withTempValue(resultTemp -> lengthIr)

    case ps.LoadVectorElement(resultTemp, vectorTemp, indexTemp) =>
      val indexIr = state.liveTemps(indexTemp)
      val vectorIr = state.liveTemps(vectorTemp)

      val block = state.currentBlock
      val elementsIr = ct.VectorCell.genLoadFromElements(block)(vectorIr)
      val elementIr = GenVector.loadElement(block)(elementsIr, indexIr)

      state.withTempValue(resultTemp -> elementIr)

    case ps.StoreVectorElement(vectorTemp, indexTemp, newValueTemp) =>
      val vectorIr = state.liveTemps(vectorTemp)
      val indexIr = state.liveTemps(indexTemp)
      val newValueIr = state.liveTemps(newValueTemp)

      val block = state.currentBlock
      val elementsIr = ct.VectorCell.genLoadFromElements(block)(vectorIr)
      GenVector.storeElement(block)(elementsIr, indexIr, newValueIr)
      state

    case ps.LoadProcedureEntryPoint(resultTemp, procTemp, signature) =>
      val procIr = state.liveTemps(procTemp)
      val signatureIrType =  ProcedureSignatureToIr(signature).irSignature.irType
      val block = state.currentBlock

      // Load the entry point
      val entryPointIr = ct.ProcedureCell.genLoadFromEntryPoint(block)(procIr)
      // Cast it to the expected entry point type
      val castEntryPointIr = block.bitcastTo("castEntryPoint")(entryPointIr, PointerType(signatureIrType))

      state.withTempValue(resultTemp -> castEntryPointIr)

    case initStep: ps.InitRecord =>
      val (initedState, resultIr) = GenInitRecordLike(state, genGlobals.generatedTypes)(initStep)

      initedState.withTempValue(initStep.result -> resultIr)

    case initStep: ps.InitInternalProc =>
      val (initedState, resultIr) = GenInitRecordLike(state, genGlobals.generatedTypes)(initStep)

      initedState.withTempValue(initStep.result -> resultIr)

    case initStep: ps.InitAdapterProc =>
      val (initedState, resultIr) = GenInitRecordLike(state, genGlobals.generatedTypes)(initStep)

      // Store the entry point
      val entryPointIr = state.liveTemps(initStep.entryPoint)
      val block = state.currentBlock

      val castEntryPointIr = block.bitcastTo("castEntryPoint")(entryPointIr, ct.ProcedureCell.entryPointIrType)
      ct.ProcedureCell.genStoreToEntryPoint(state.currentBlock)(castEntryPointIr, resultIr)

      initedState.withTempValue(initStep.result -> resultIr)

    case ps.TestRecordLikeClass(resultTemp, recordCellTemp, recordLikeType, possibleTypesOpt) =>
      val generatedType = genGlobals.generatedTypes(recordLikeType)
      val generatedPossibleTypes = possibleTypesOpt map { possibleTypes =>
        possibleTypes.map(genGlobals.generatedTypes)
      }

      val recordCellIr = state.liveTemps(recordCellTemp)
      val irResult = GenTestRecordLikeClass(state.currentBlock)(recordCellIr, generatedType, generatedPossibleTypes)

      state.withTempValue(resultTemp -> irResult)

    case ps.SetRecordLikeDefined(recordCellTemp, recordLikeType) =>
      val generatedType = genGlobals.generatedTypes(recordLikeType)

      // Mark ourselves as defined
      val recordCellIr = state.liveTemps(recordCellTemp)
      val cellType = generatedType.recordLikeType.cellType
      cellType.genStoreToIsUndefined(state.currentBlock)(IntegerConstant(cellType.isUndefinedIrType, 0), recordCellIr)

      state

    case ps.AssertRecordLikeDefined(recordCellTemp, recordLikeType, errorMessage) =>
      val worldPtrIr = state.liveTemps(ps.WorldPtrValue)
      val generatedType = genGlobals.generatedTypes(recordLikeType)

      // Start our branches
      val irFunction = state.currentBlock.function
      val fatalBlock = irFunction.startChildBlock("recordIsUndefined")
      val successBlock = irFunction.startChildBlock("recordIsDefined")

      GenErrorSignal(state.copy(currentBlock=fatalBlock))(worldPtrIr, errorMessage, locatedOpt=Some(step))

      // Branch if we're defined
      val recordCellIr = state.liveTemps(recordCellTemp)
      val cellType = generatedType.recordLikeType.cellType

      val loadMetadata = Map("range" -> RangeMetadata(cellType.isUndefinedIrType, (0, 2)))
      val isUndefinedBool = cellType.genLoadFromIsUndefined(state.currentBlock)(recordCellIr, loadMetadata)

      val isUndefinedPred = state.currentBlock.truncTo("undefinedPred")(isUndefinedBool, IntegerType(1))

      state.currentBlock.condBranch(isUndefinedPred, fatalBlock, successBlock)

      // Continue with the successful block
      state.copy(currentBlock=successBlock)

    case ps.SetRecordLikeFields(recordTemp, recordType, fieldsToSet) =>
      val recordIr = state.liveTemps(recordTemp)
      val generatedType = genGlobals.generatedTypes(recordType)
      val recordDataIr = GenLoadRecordLikeData(state.currentBlock)(recordIr, generatedType)

      val fieldsToSetIr = fieldsToSet.map { case (newValueTemp, recordField) =>
        (state.liveTemps(newValueTemp), recordField)
      }

      GenSetRecordLikeFields(state.currentBlock)(recordDataIr, generatedType, fieldsToSetIr)

      state

    case ps.LoadRecordLikeFields(recordTemp, recordLikeType, fieldsToLoad) =>
      val recordIr = state.liveTemps(recordTemp)
      val generatedType = genGlobals.generatedTypes(recordLikeType)
      val recordDataIr = GenLoadRecordLikeData(state.currentBlock)(recordIr, generatedType)

      val resultIrs = GenLoadRecordLikeFields(state.currentBlock)(recordDataIr, generatedType, fieldsToLoad.map(_._1))

      val tempValuesToIr = fieldsToLoad.zip(resultIrs)

      tempValuesToIr.foldLeft(state) { case (state, ((field, tempValue), irValue)) =>
        state.withTempValue(tempValue -> irValue)
      }

    case pushDynamic: ps.PushDynamicState =>
      GenParameter.genPushDynamicState(state)(pushDynamic)

    case popDynamic: ps.PopDynamicState =>
      GenParameter.genPopDynamicState(state)(popDynamic)

    case ps.CreateParameterProc(resultTemp, initialValueTemp) =>
      val worldPtrIr = state.liveTemps(ps.WorldPtrValue)
      val initialValueIr = state.liveTemps(initialValueTemp)

      val resultIr = GenParameter.genCreateParameterProc(state)(
        worldPtrIr,
        initialValueIr
      )

      state.withTempValue(resultTemp -> resultIr)

    case ps.LoadValueForParameterProc(resultTemp, parameterProcTemp) =>
      val worldPtrIr = state.liveTemps(ps.WorldPtrValue)
      val parameterProcIr = state.liveTemps(parameterProcTemp)

      val resultIr = GenParameter.genLoadValueForParameterProc(state)(worldPtrIr, parameterProcIr)
      state.withTempValue(resultTemp -> resultIr)

    case checkedStep @ ps.CheckedIntegerAdd(resultTemp, val1Temp, val2Temp, overflowMessage) =>
      val worldPtrIr = state.liveTemps(ps.WorldPtrValue)
      val val1Ir = state.liveTemps(val1Temp)
      val val2Ir = state.liveTemps(val2Temp)

      val (newState, resultIr) = GenCheckedIntegerInstr(state)(worldPtrIr, checkedStep, "add", val1Ir, val2Ir)

      newState.withTempValue(resultTemp -> resultIr)

    case checkedStep @ ps.CheckedIntegerSub(resultTemp, val1Temp, val2Temp, overflowMessage) =>
      val worldPtrIr = state.liveTemps(ps.WorldPtrValue)
      val val1Ir = state.liveTemps(val1Temp)
      val val2Ir = state.liveTemps(val2Temp)

      val (newState, resultIr) = GenCheckedIntegerInstr(state)(worldPtrIr, checkedStep, "sub", val1Ir, val2Ir)

      newState.withTempValue(resultTemp -> resultIr)

    case checkedStep @ ps.CheckedIntegerMul(resultTemp, val1Temp, val2Temp, overflowMessage) =>
      val worldPtrIr = state.liveTemps(ps.WorldPtrValue)
      val val1Ir = state.liveTemps(val1Temp)
      val val2Ir = state.liveTemps(val2Temp)

      val (newState, resultIr) = GenCheckedIntegerInstr(state)(worldPtrIr, checkedStep, "mul", val1Ir, val2Ir)

      newState.withTempValue(resultTemp -> resultIr)

    case ps.IntegerDiv(resultTemp, signed, val1Temp, val2Temp) =>
      val val1Ir = state.liveTemps(val1Temp)
      val val2Ir = state.liveTemps(val2Temp)

      val resultIr = if (signed) {
        state.currentBlock.sdiv("sdivResult")(false, val1Ir, val2Ir)
      }
      else {
        state.currentBlock.udiv("udivResult")(false, val1Ir, val2Ir)
      }

      state.withTempValue(resultTemp -> resultIr)

    case ps.IntegerRem(resultTemp, signed, val1Temp, val2Temp) =>
      val val1Ir = state.liveTemps(val1Temp)
      val val2Ir = state.liveTemps(val2Temp)

      val resultIr = if (signed) {
        state.currentBlock.srem("sremResult")(val1Ir, val2Ir)
      }
      else {
        state.currentBlock.urem("uremResult")(val1Ir, val2Ir)
      }

      state.withTempValue(resultTemp -> resultIr)

    case ps.FloatAdd(resultTemp, val1Temp, val2Temp) =>
      val val1Ir = state.liveTemps(val1Temp)
      val val2Ir = state.liveTemps(val2Temp)

      val resultIr = state.currentBlock.fadd("faddResult")(fastMathFlags, val1Ir, val2Ir)

      state.withTempValue(resultTemp -> resultIr)

    case ps.FloatSub(resultTemp, val1Temp, val2Temp) =>
      val val1Ir = state.liveTemps(val1Temp)
      val val2Ir = state.liveTemps(val2Temp)

      val resultIr = state.currentBlock.fsub("fsubResult")(fastMathFlags, val1Ir, val2Ir)

      state.withTempValue(resultTemp -> resultIr)

    case ps.FloatMul(resultTemp, val1Temp, val2Temp) =>
      val val1Ir = state.liveTemps(val1Temp)
      val val2Ir = state.liveTemps(val2Temp)

      val resultIr = state.currentBlock.fmul("fmulResult")(fastMathFlags, val1Ir, val2Ir)

      state.withTempValue(resultTemp -> resultIr)

    case ps.FloatDiv(resultTemp, val1Temp, val2Temp) =>
      val val1Ir = state.liveTemps(val1Temp)
      val val2Ir = state.liveTemps(val2Temp)

      val resultIr = state.currentBlock.fdiv("fdivResult")(fastMathFlags, val1Ir, val2Ir)

      state.withTempValue(resultTemp -> resultIr)

    case ps.IntegerCompare(resultTemp, compareCond, signed, val1Temp, val2Temp) =>
      val val1Ir = state.liveTemps(val1Temp)
      val val2Ir = state.liveTemps(val2Temp)

      val condIr = stepCompareCondToIntegerIr(compareCond)
      val resultIr = state.currentBlock.icmp("compResult")(condIr, signed, val1Ir, val2Ir)

      state.withTempValue(resultTemp -> resultIr)

    case ps.FloatCompare(resultTemp, compareCond, val1Temp, val2Temp) =>
      val val1Ir = state.liveTemps(val1Temp)
      val val2Ir = state.liveTemps(val2Temp)

      val condIr = stepCompareCondToFloatIr(compareCond)
      val resultIr = state.currentBlock.fcmp("compResult")(condIr, val1Ir, val2Ir)

      state.withTempValue(resultTemp -> resultIr)

    case ps.FloatIsNaN(resultTemp, valTemp) =>
      val valIr = state.liveTemps(valTemp)

      // This is tricky - only NaN is not equal to itself. This is actually now isnan is implemented on OS X and LLVM
      // optimises it appropriately
      val resultIr = state.currentBlock.fcmp("isNaN")(FComparisonCond.UnorderedNotEqual, valIr, valIr)

      state.withTempValue(resultTemp -> resultIr)

    case ps.FloatBitwiseCompare(resultTemp, val1Temp, val2Temp) =>
      val val1Ir = state.liveTemps(val1Temp)
      val val2Ir = state.liveTemps(val2Temp)

      val block = state.currentBlock
      val val1IntCastIr = block.bitcastTo("val1IntCast")(val1Ir, IntegerType(64))
      val val2IntCastIr = block.bitcastTo("val2IntCast")(val2Ir, IntegerType(64))

      val resultIr = block.icmp("compResult")(IComparisonCond.Equal, None, val1IntCastIr, val2IntCastIr)
      state.withTempValue(resultTemp -> resultIr)

    case ps.InitPair(resultTemp, carValueTemp, cdrValueTemp, listLengthOpt, stackAllocate) =>
      val block = state.currentBlock
      val allocation = state.currentAllocation

      val (newAllocation, resultIr) = if (stackAllocate) {
        (allocation, GenStackAllocation(block)(ct.PairCell))
      }
      else {
        allocation.consumeCells(block)(1, ct.PairCell)
      }

      // List length of zero means unknown
      val listLengthToStore = listLengthOpt.getOrElse(0L)
      val listLengthIr = IntegerConstant(ct.PairCell.listLengthIrType, listLengthToStore)
      ct.PairCell.genStoreToListLength(state.currentBlock)(listLengthIr, resultIr)

      val carValueIr = state.liveTemps(carValueTemp)
      ct.PairCell.genStoreToCar(state.currentBlock)(carValueIr, resultIr)

      val cdrValueIr = state.liveTemps(cdrValueTemp)
      ct.PairCell.genStoreToCdr(state.currentBlock)(cdrValueIr, resultIr)

      state.copy(
        currentAllocation=newAllocation
      ).withTempValue(resultTemp -> resultIr)

    case ps.AssertPredicate(predicateTemp, errorMessage, evidenceOpt) =>
      val worldPtrIr = state.liveTemps(ps.WorldPtrValue)
      val predIr = state.liveTemps(predicateTemp)
      val evidenceIrOpt = evidenceOpt.map(state.liveTemps)

      // Start our branches
      val irFunction = state.currentBlock.function
      val fatalBlock = irFunction.startChildBlock("predFalse")
      val successBlock = irFunction.startChildBlock("predTrue")

      GenErrorSignal(state.copy(currentBlock=fatalBlock))(worldPtrIr, errorMessage, evidenceIrOpt, locatedOpt=Some(step))

      state.currentBlock.condBranch(predIr, successBlock, fatalBlock)

      // Continue with the successful block
      state.copy(currentBlock=successBlock)
  }
}
