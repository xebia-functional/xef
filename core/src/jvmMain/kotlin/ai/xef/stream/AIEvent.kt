package ai.xef.stream

import ai.xef.response.Response

sealed interface AIEvent<out A> {
  data class Chunk(val chunk: String) : AIEvent<Nothing>
  data class Error(val error: Throwable) : AIEvent<Nothing>
  data class Complete<A>(val response: Response<A>) : AIEvent<A>
  data class ToolRequest(
    val id: String,
    val name: String,
    val arguments: String
  ) : AIEvent<Nothing>
  data class ToolResult(
    val id: String,
    val toolName: String,
    val text: String
  ) : AIEvent<Nothing>
}
