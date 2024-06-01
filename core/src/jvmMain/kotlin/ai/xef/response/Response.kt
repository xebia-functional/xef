package ai.xef.response

data class Response<out A>(
  val content: A,
  val tokenUsage: TokenUsage,
  val finishReason: FinishReason
)
