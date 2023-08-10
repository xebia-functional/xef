package com.xebia.functional.xef.reasoning.tools

import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.models.chat.Message
import io.github.oshai.kotlinlogging.KotlinLogging

class ToolSelection(
  private val model: ChatWithFunctions,
  private val scope: Conversation,
  private val tools: List<Tool>,
  private val instructions: List<String> = emptyList()
) : Tool {

  private val logger = KotlinLogging.logger {}

  override val name: String = "Tool Selection"

  override val description: String = "Select the best tool for the job"

  override suspend fun invoke(input: String): String {
    return when (val trace = applyInferredTools(input)) {
      is ToolsExecutionTrace.Completed -> trace.output
      ToolsExecutionTrace.Empty -> ""
    }
  }

  suspend fun applyInferredTools(task: String): ToolsExecutionTrace {
    logger.info { "🔍 Applying inferred tools for task: $task" }
    val plan = createExecutionPlan(task)
    logger.info { "🔍 Applying execution plan with reasoning: ${plan.reasoning}" }
    val stepsAndTools =
      plan.steps.mapNotNull { step ->
        val tool = tools.find { it.name == step.tool.name }
        tool?.let { step to it }
      }
    return when {
      stepsAndTools.isEmpty() -> ToolsExecutionTrace.Empty
      stepsAndTools.size == 1 -> {
        val (step, tool) = stepsAndTools.first()
        applyToolOnStep(tool, step, "", task)
      }
      else -> {
        stepsAndTools.foldIndexed(ToolsExecutionTrace.Empty as ToolsExecutionTrace) {
          index,
          acc,
          (step, tool) ->
          when (acc) {
            is ToolsExecutionTrace.Empty -> applyToolOnStep(tool, step, "", task)
            is ToolsExecutionTrace.Completed -> {
              val previousOutput = acc.output
              val result = applyToolOnStep(tool, step, previousOutput, task)
              acc.copy(results = acc.results + result.results, output = result.output)
            }
          }
        }
      }
    }
  }

  private suspend fun applyToolOnStep(
    tool: Tool,
    step: ToolExecutionStep,
    previousOutput: String,
    input: String,
  ): ToolsExecutionTrace.Completed {
    logger.info { "🔍 Applying tool: ${tool.name} for step: ${step.reasoning}" }
    val prompt =
      """|
      |Previous knowledge:
      |$previousOutput
      |original input:
      |$input
    """
        .trimMargin()
    val output = tool.invoke(prompt)
    return ToolsExecutionTrace.Completed(results = mapOf(step to output), output = output)
  }

  suspend fun createExecutionPlan(task: String): ToolsExecutionPlan {
    logger.info { "🔍 Creating execution plan for task: $task" }

    val messages: List<Message> =
      listOf(
        Message.systemMessage {
          "You are an expert in tool selection that can choose the best tools for a specific task based on the tools descriptions"
        },
        Message.assistantMessage { "Given the following task:" },
        Message.assistantMessage { task },
        Message.assistantMessage { "Given the following tools:" },
      ) +
        tools.map { Message.assistantMessage { "${it.name}: ${it.description}" } } +
        listOf(
          Message.userMessage { "Follow the next instructions" },
          Message.userMessage {
            "Select the best execution plan with tools for the `task` based on the `tools`"
          },
          Message.userMessage {
            "Your `RESPONSE` MUST be a `ToolsExecutionPlan` object, where the `steps` determine how the execution plan will run the tools"
          },
        ) +
        instructions.map { Message.userMessage { it } }

    return model.prompt(
      scope = scope,
      serializer = ToolsExecutionPlan.serializer(),
      messages = messages
    )
  }
}
