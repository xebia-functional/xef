package com.xebia.functional.xef.reasoning.tools

import com.xebia.functional.xef.reasoning.internals.callModel
import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import io.github.oshai.kotlinlogging.KotlinLogging

class ToolSelection(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope
) {

  private val logger = KotlinLogging.logger {}

  suspend fun selectTool(task: String, tools: List<Tool>): ToolSelectionResult {
    logger.info { "🔍 Selecting tool for task: $task" }
    return callModel<ToolSelectionResult>(
      model,
      scope,
      prompt = ExpertSystem(
        system = "You are an expert in tool selection that can choose the best tool for a specific task based on the tool descriptions",
        query = """|
                |Given the following task:
                |```task
                |${task}
                |```
                |And the following tools:
                |```tools
                |${tools.joinToString("\n") { "${it.name}: ${it.description}" }}
                |```
            """.trimMargin(),
        instructions = listOf(
          "Select the best tool for the `task` based on the `tools`",
          "Your `RESPONSE` MUST be a `ToolSelectionResult` object, where the `tool` is the selected tool"
        )
      )
    ).also {
      logger.info { "🔍 Tool selection result: $it" }
    }
  }
}
