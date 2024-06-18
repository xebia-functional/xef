package com.xebia.functional.xef

sealed class AIEvent<out A> {
  data object Start : AIEvent<Nothing>()

  data class Result<out A>(val value: A) : AIEvent<A>()

  data class ToolExecutionRequest(val tool: Tool<*>, val input: Any?) : AIEvent<Nothing>()

  data class ToolExecutionResponse(val tool: Tool<*>, val output: Any?) : AIEvent<Nothing>()

  data class Stop(val usage: Usage) : AIEvent<Nothing>() {
    data class Usage(
      val llmCalls: Int,
      val toolCalls: Int,
      val inputTokens: Int,
      val outputTokens: Int,
      val totalTokens: Int,
    )
  }

  fun debugPrint(): Unit =
    when (this) {
      // emoji for start is: 🚀
      Start -> println("🚀 Starting...")
      is Result -> println("🎉 $value")
      is ToolExecutionRequest ->
        println("🔧 Executing tool: ${tool.function.name} with input: $input")
      is ToolExecutionResponse ->
        println("🔨 Tool response: ${tool.function.name} resulted in: $output")
      is Stop -> {
        println("🛑 Stopping...")
        println("📊 Usage: $usage")
      }
    }
}
