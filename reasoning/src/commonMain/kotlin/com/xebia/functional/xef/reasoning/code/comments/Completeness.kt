package com.xebia.functional.xef.reasoning.code.comments

import kotlinx.serialization.Serializable

@Serializable
enum class Completeness {
  COMPLETE,
  PARTIAL,
  INCOMPLETE
}
