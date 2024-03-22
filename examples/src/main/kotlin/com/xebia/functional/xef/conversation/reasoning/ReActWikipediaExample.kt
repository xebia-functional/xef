package com.xebia.functional.xef.conversation.reasoning

import ai.xef.openai.StandardModel
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.reasoning.tools.LLMTool
import com.xebia.functional.xef.reasoning.tools.ReActAgent
import com.xebia.functional.xef.reasoning.wikipedia.SearchWikipedia
import com.xebia.functional.xef.reasoning.wikipedia.SearchWikipediaByPageId
import com.xebia.functional.xef.reasoning.wikipedia.SearchWikipediaByTitle

suspend fun main() {
  Conversation {
    val model = StandardModel(CreateChatCompletionRequestModel.gpt_3_5_turbo)
    val serialization = StandardModel(CreateChatCompletionRequestModel.gpt_3_5_turbo_16k_0613)
    val math =
      LLMTool.create(
        name = "Calculator",
        description = "Math operations expressed in numbers and math symbols",
        model = model,
        scope = this
      )
    val search = SearchWikipedia(model = model, scope = this)
    val searchByPageId = SearchWikipediaByPageId(model = model, scope = this)
    val searchByTitle = SearchWikipediaByTitle(model = model, scope = this)

    val reActAgent =
      ReActAgent(
        model = serialization,
        scope = this,
        tools = listOf(search, math, searchByPageId, searchByTitle),
      )
    val result =
      reActAgent.run(
        "Find and multiply the number of human bones by the number of Metallica albums"
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
