package ai.xef.response

data class TokenUsage(
  val inputTokens: Int = 0,
  val outputTokens: Int = 0,
  val totalTokens: Int = 0
)
