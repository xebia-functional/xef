package com.xebia.functional.xef.reasoning.code.refactor

import kotlinx.serialization.Serializable

@Serializable
data class RefactoringResult(
  val refactoredCode: String,
)
