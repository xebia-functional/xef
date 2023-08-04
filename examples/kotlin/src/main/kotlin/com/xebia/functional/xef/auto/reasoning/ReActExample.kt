package com.xebia.functional.xef.auto.reasoning

import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.getOrThrow
import com.xebia.functional.xef.reasoning.search.Search
import com.xebia.functional.xef.reasoning.tools.LLMTool
import com.xebia.functional.xef.reasoning.tools.ReActAgent

suspend fun main() {
  ai {
    val model = OpenAI.DEFAULT_CHAT
    val serialization = OpenAI.DEFAULT_SERIALIZATION
    val math = LLMTool.create(
      name = "Calculator",
      description = "Perform math operations and calculations processing them with an LLM model. The tool input is a simple string containing the operation to solve expressed in numbers and math symbols.",
      model = model,
      scope = this
    )
    val search = Search(model = model, scope = this)

    val reActAgent = ReActAgent(
      model = serialization,
      scope = this,
      tools = listOf(
        math,
        search,
      ),
    )

    val result =
      reActAgent.run("Who is Leonardo di Caprio? Who is his girlfriend? How old is she?")
    println(result)
  }.getOrThrow()
}
