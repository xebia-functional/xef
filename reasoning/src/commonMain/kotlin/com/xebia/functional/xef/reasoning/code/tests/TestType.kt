package com.xebia.functional.xef.reasoning.code.tests

import kotlinx.serialization.Serializable

@Serializable
enum class TestType {
  UNIT,
  INTEGRATION,
  LAWS,
  PERFORMANCE
}
