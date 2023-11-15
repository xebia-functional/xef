package com.xebia.functional.xef.conversation.tot

// Function to truncate a text to fit within a certain character limit
fun truncateText(text: String, limit: Int = 150): String {
  return if (text.length > limit) {
      text.substring(0, limit - 3) + "..."
    } else {
      text
    }
    .replace("\n", " ")
}

internal fun renderHistory(memory: Memory<*>): String =
  """|```history
    |${
  memory.history.joinToString("\n") {
    renderHistoryItem(it)
  }
}
    |```"""
    .trimMargin()

private fun renderHistoryItem(it: Solution<*>) =
  """|
           |${it.answer}
           |${it.reasoning}
           |${if (it.isValid) "✅" else "❌"}"""
    .trimMargin()
