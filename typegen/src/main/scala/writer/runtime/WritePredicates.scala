package io.llambda.typegen.writer.runtime

import io.llambda.typegen._

object WritePredicates extends writer.OutputWriter {
  def apply(processedTypes : ProcessedTypes) : Map[String, String] = {
    // Get a list of non-internal cell classes
    val publicCellClasses = processedTypes.cellClasses.values.filter(!_.internal)
    
    // Get the C++ name for the root class
    val rootClassCppName = CellClassNames(processedTypes.rootCellClass.name).cppClassName

    // Start our builder
    val cppBuilder = new CppBuilder

    cppBuilder.appendRaw(writer.GeneratedClikeFileComment)

    for(cellClass <- publicCellClasses) {
      cppBuilder += "#include \"binding/" + cellClass.names.cppClassName + ".h\""
    }

    cppBuilder.sep()
    cppBuilder += "using namespace lliby;"
    cppBuilder.sep()
    cppBuilder += "extern \"C\""
    cppBuilder += "{"
    cppBuilder.sep()

    for(cellClass <- publicCellClasses) {
      val functionName = cellClass.names.predicateFunctionName
      cppBuilder += s"bool ${functionName}(const ${rootClassCppName} *value)"

      cppBuilder.blockSep {
        cppBuilder += s"return ${cellClass.names.cppClassName}::isInstance(value);"
      }
    }
    
    cppBuilder.sep()
    cppBuilder += "}"

    Map("runtime/stdlib/generated/predicates.cpp" -> cppBuilder.toString)
  }
}
