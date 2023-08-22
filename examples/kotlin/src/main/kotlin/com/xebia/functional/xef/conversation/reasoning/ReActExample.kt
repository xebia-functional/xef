package com.xebia.functional.xef.conversation.reasoning

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.serpapi.Search
import com.xebia.functional.xef.reasoning.tools.LLMTool
import com.xebia.functional.xef.reasoning.tools.ReActAgent
import com.xebia.functional.xef.reasoning.tools.ReactAgentEvents
import com.xebia.functional.xef.tracing.ToolEvent
import com.xebia.functional.xef.tracing.Tracker
import com.xebia.functional.xef.tracing.createDispatcher

suspend fun main() {
  val tracker = Tracker<ReactAgentEvents> {
    when (this) {
      is ReactAgentEvents.FinalAnswer -> "🎉 $answer"
      is ReactAgentEvents.MaxIterationsReached -> "🤷‍ Max iterations reached : $maxIterations"
      is ReactAgentEvents.Observation -> "👀 $value"
      is ReactAgentEvents.SearchingTool -> "🛠 $tool [${input}]"
      is ReactAgentEvents.Thinking -> "🤔 $though"
      is ReactAgentEvents.ToolNotFound -> "🤷‍ Could not find tool $tool"
    }.also (::println)
  }

  OpenAI.conversation(dispatcher = createDispatcher(tracker)) {
    val model = OpenAI().DEFAULT_CHAT
    val serialization = OpenAI().DEFAULT_SERIALIZATION
    val math =
      LLMTool.create(
        name = "Calculator",
        description = "Perform math operations and calculations processing them with an LLM model. The tool input is a simple string containing the operation to solve expressed in numbers and math symbols.",
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
        Prompt {
          +user(
            "Find and multiply the number of Leonardo di Caprio's girlfriends by the number of Metallica albums"
          )
        }
      )
    println(result)
  }
}
