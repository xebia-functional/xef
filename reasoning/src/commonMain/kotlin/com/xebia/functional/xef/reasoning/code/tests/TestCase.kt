package com.xebia.functional.xef.reasoning.code.tests

import kotlinx.serialization.Serializable

@Serializable
data class TestCase(
  val type: TestType,
  val description: String,
  val code: String,
)
