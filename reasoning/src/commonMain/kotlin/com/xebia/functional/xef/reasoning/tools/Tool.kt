package com.xebia.functional.xef.reasoning.tools

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

  suspend fun handle(input: ToolOutput<Any?>): Out<A>?

  val functions: Map<ToolMetadata, suspend (input: String) -> Out<A>>
}
