package ai.xef.response

enum class FinishReason {
  STOP,
  LENGTH,
  TOOL_EXECUTION,
  CONTENT_FILTER,
  OTHER
}
