package com.xebia.functional.xef.reasoning.tools

interface Tool : ToolMain {
  suspend operator fun invoke(input: String): String
}
