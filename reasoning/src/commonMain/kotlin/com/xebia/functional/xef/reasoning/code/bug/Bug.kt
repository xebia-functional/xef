package com.xebia.functional.xef.reasoning.code.bug

import kotlinx.serialization.Serializable

@Serializable
data class Bug(
  val line: Int,
  val category: BugCategory,
  val description: String,
)
