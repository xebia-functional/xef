package com.xebia.functional.xef.evaluator.errors

sealed interface IOError {
  fun message(prefix: String): String
}

data class FileNotFound(val fileName: String) : IOError {
  override fun message(prefix: String): String = "$prefix $fileName not found"
}
