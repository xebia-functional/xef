package com.xebia.functional.xef.reasoning.code.bug

import kotlinx.serialization.Serializable

@Serializable
enum class BugCategory {
  SYNTAX,
  LOGIC,
  PERFORMANCE,
  CONCURRENCY,
  SECURITY,
  MEMORY,
  EXCEPTION_HANDLING
}
