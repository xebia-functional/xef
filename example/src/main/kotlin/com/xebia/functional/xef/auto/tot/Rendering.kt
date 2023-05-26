package com.xebia.functional.xef.auto.tot

// Function to truncate a text to fit within a certain character limit
fun truncateText(text: String, limit: Int = 150): String {
  return if (text.length > limit) {
    text.substring(0, limit - 3) + "..."
  } else {
    text
  }.replace("\n", " ")
}

internal fun remindJSONSchema(): String =
  """|IMPORTANT INSTRUCTIONS:
     |1. Provide your response in application/json output in the json schema provided below.
     |2. If you don't provide your response in the json schema provided below, 
     |   the program will fail and someone may get hurt.
     |3. I repeat, if you don't provide your response in the json schema provided below,
     |   the program will fail and someone may get hurt.
     |""".trimMargin()

internal fun renderHistory(memory: Memory<*>): String = """|```history
    |${
  memory.history.joinToString("\n") {
    renderHistoryItem(it)
  }
}
    |```""".trimMargin()

private fun renderHistoryItem(it: Solution<*>) = """|
           |${it.answer}
           |${it.reasoning}
           |${if (it.isValid) "✅" else "❌"}""".trimMargin()
