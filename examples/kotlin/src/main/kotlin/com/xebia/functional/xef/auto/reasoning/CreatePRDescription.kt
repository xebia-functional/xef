package com.xebia.functional.xef.auto.reasoning

import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.reasoning.code.Code
import com.xebia.functional.xef.reasoning.tools.ReActAgent

suspend fun main() {
  OpenAI.conversation {
    val code =
      Code(
        model = OpenAI().DEFAULT_CHAT,
        serialization = OpenAI().DEFAULT_SERIALIZATION,
        scope = this
      )

    val agent =
      ReActAgent(
        model = OpenAI().DEFAULT_SERIALIZATION,
        scope = this,
        tools = listOf(code.diffSummaryFromUrl)
      )
    val prDescription =
      agent.run(
        listOf(
          Message.userMessage {
            "Create a PR description for https://patch-diff.githubusercontent.com/raw/xebia-functional/xef/pull/283.diff"
          }
        )
      )
    println(prDescription)
  }
}
