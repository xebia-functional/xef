package com.xebia.functional.xef.reasoning.tools

data class ToolOutput<A>(val metadata: ToolMetadata, val output: List<String>, val value: A) {
  inline fun <reified B> valueOrNull(): B? = value as? B
}

interface Tool<A> {
  interface Out<A> {
    fun toolOutput(metadata: ToolMetadata): ToolOutput<A>

    companion object {
      fun <A> empty() =
        object : Out<A?> {
          override fun toolOutput(metadata: ToolMetadata): ToolOutput<A?> =
            ToolOutput(metadata, emptyList(), null)
        }
    }
  }

  val functions: Map<ToolMetadata, suspend (input: String) -> Out<A>>
}
