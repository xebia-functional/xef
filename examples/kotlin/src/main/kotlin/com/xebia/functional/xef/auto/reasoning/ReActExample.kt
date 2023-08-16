package com.xebia.functional.xef.auto.reasoning

import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.prompt.buildPrompt
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.serpapi.Search
import com.xebia.functional.xef.reasoning.tools.LLMTool
import com.xebia.functional.xef.reasoning.tools.ReActAgent

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
    val search = Search(model = model, scope = this)

    val reActAgent =
      ReActAgent(
        model = serialization,
        scope = this,
        tools =
          listOf(
            search,
            math,
          ),
      )
    val result =
      reActAgent.run(
        buildPrompt {
          +user(
            "Find and multiply the number of Leonardo di Caprio's girlfriends by the number of Metallica albums"
          )
        }
      )
    println(result)
  }
}
