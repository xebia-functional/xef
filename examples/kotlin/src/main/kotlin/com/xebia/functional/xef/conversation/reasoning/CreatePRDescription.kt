package com.xebia.functional.xef.conversation.reasoning

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.reasoning.code.Code
import com.xebia.functional.xef.reasoning.tools.ReActAgent
import kotlinx.coroutines.flow.collect

suspend fun main() {
  OpenAI.conversation {
    val code =
      Code(
        model = OpenAI.fromEnvironment().DEFAULT_CHAT,
        serialization = OpenAI.fromEnvironment().DEFAULT_SERIALIZATION,
        scope = this
      )

    val agent =
      ReActAgent(
        model = OpenAI.fromEnvironment().DEFAULT_SERIALIZATION,
        scope = this,
        tools = listOf(code.diffSummaryFromUrl)
      )
    val prDescription =
      agent.run(
        "Create a PR description for https://patch-diff.githubusercontent.com/raw/xebia-functional/xef/pull/283.diff"
      )
    prDescription.collect {
      when (it) {
        is ReActAgent.Result.Log -> println(it.message)
        is ReActAgent.Result.ToolResult -> println("${it.tool}(${it.input}) = ${it.result}")
        is ReActAgent.Result.Finish -> println(it.result)
        is ReActAgent.Result.MaxIterationsReached -> println(it.message)
      }
    }
  }
}
