package com.xebia.functional.xef.evaluator.models.errors

sealed interface ValidationError {
  val message: String
}

sealed class SuiteSpecError(override val message: String) : ValidationError

data object EmptyOutputDescription :
  SuiteSpecError("Output description is empty, please provide a description")

data class EmptySuiteSpecOutputDescription(val index: Int = 0) :
  SuiteSpecError(
    "SuiteSpec output description at index $index is empty, please provide a description",
  )

data object EmptySuiteSpecDescription :
  SuiteSpecError("SuiteSpec description is empty, please provide a description")

data class EmptyItemSpecOutputResponse(val index: Int) :
  SuiteSpecError(
    "Empty output response at the ItemSpec: $index, please provide an output response"
  )

data class InvalidNumberOfItemSpecOutputResponse(val index: Int) :
  SuiteSpecError(
    """The number of outputs response are invalid at index $index, 
    |please provide the same number of outputs response and outputs description"""
      .trimMargin()
  )

data object OutputsDescriptionNotProvided :
  SuiteSpecError(
    "SuiteSpec outputs description is not provided, please provide at least one output description"
  )

data object MoreOutputsDescriptionThanItemsSpec :
  SuiteSpecError(
    """The number of outputs description are greater than the number of items spec, 
    |please provide the same number of outputs description and items spec"""
      .trimMargin()
  )

data object LessOutputsDescriptionThanItemsSpec :
  SuiteSpecError(
    """The number of outputs description are less than the number of items spec, 
    |please provide the same number of outputs description and items spec"""
      .trimMargin()
  )

data object EmptyOutputResponse :
  SuiteSpecError("Output response is empty, please provide a response")

data object EmptyContextDescription :
  SuiteSpecError("Context description is empty, please provide a description")

data object EmptyItemSpecInput :
  SuiteSpecError("TestSpecItem input is empty, please provide an input")
