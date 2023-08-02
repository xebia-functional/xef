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
      description = "Resolve math operations",
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
      reActAgent.run("Gather the names of all citizens in the world")
    println(result)
  }.getOrThrow()
}
