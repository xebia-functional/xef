package com.xebia.functional.xef.reasoning.tools

data class ToolOutput<A>(val metadata: ToolMetadata, val output: List<String>, val value: A) {
  inline fun <reified B> valueOrNull(): B? = value as? B
  fun toOutputString(): String = value as? String ?: output.joinToString("\n")
}
