package com.xebia.functional.xef.reasoning.tools

interface Tool {
  val name: String
  val description: String

  suspend operator fun invoke(input: String): String
}
