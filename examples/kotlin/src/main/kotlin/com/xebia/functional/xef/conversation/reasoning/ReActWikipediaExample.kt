package com.xebia.functional.xef.conversation.reasoning

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.tools.LLMTool
import com.xebia.functional.xef.reasoning.tools.ReActAgent
import com.xebia.functional.xef.reasoning.wikipedia.*

suspend fun main() {
  OpenAI.conversation {
    val model = OpenAI().DEFAULT_CHAT
    val serialization = OpenAI().DEFAULT_SERIALIZATION
    val math =
      LLMTool.create(
        name = "Calculator",
        description =
          "Perform math operations and calculations processing them with an LLM model. The tool input is a simple string containing the operation to solve expressed in numbers and math symbols.",
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
        Prompt {
          +user("Find and multiply the number of human bones by the number of Metallica albums")
        }
      )
    println(result)
  }
}
