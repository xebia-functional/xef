package com.xebia.functional.xef.conversation.reasoning

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.user
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
        Prompt {
          +user(
            "Create a PR description for https://patch-diff.githubusercontent.com/raw/xebia-functional/xef/pull/283.diff"
          )
        }
      )
    println(prDescription)
  }
}
