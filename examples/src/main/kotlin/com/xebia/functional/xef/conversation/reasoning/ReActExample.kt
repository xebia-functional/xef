package com.xebia.functional.xef.conversation.reasoning

import ai.xef.openai.StandardModel
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.reasoning.serpapi.Search
import com.xebia.functional.xef.reasoning.tools.ReActAgent

suspend fun main() {
  Conversation {
    val model = StandardModel(CreateChatCompletionRequestModel.gpt_3_5_turbo)
    val serialization = StandardModel(CreateChatCompletionRequestModel.gpt_3_5_turbo_16k_0613)

    val search = Search(model = model, scope = this)

    val reActAgent =
      ReActAgent(
        model = serialization,
        scope = this,
        tools = listOf(search),
      )
    val result =
      reActAgent.run(
        "Find and multiply the number of days in a week by the number of months in a year. Then subtract the number of letters in the English alphabet from the result. Display the result of all operations as a single number"
      )

    result.collect {
      when (it) {
        is ReActAgent.Result.Log -> println(it.message)
        is ReActAgent.Result.ToolResult -> println("${it.tool}(${it.input}) = ${it.result}")
        is ReActAgent.Result.Finish -> println(it.result)
        is ReActAgent.Result.MaxIterationsReached -> println(it.message)
      }
    }
  }
}
