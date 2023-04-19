package llm.models

data class CompletionChoice(val text: String, val index: Int, val finishReason: String)
