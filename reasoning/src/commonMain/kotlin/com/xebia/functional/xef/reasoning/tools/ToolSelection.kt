package com.xebia.functional.xef.reasoning.tools

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user

class ToolSelection(
  private val model: ChatWithFunctions,
  private val scope: Conversation,
  private val tools: List<Tool>,
  private val instructions: List<String> = emptyList()
) : Tool {

  override val name: String = "Tool Selection"

  override val description: String = "Select the best tool for the job"

  override suspend fun invoke(input: String): String {
    return when (val trace = applyInferredTools(input)) {
      is ToolsExecutionTrace.Completed -> trace.output
      ToolsExecutionTrace.Empty -> ""
    }
  }

  suspend fun applyInferredTools(task: String): ToolsExecutionTrace {
    scope.track(TaskEvent.ApplyingTool(task))
    val plan = createExecutionPlan(task)
    scope.track(TaskEvent.ApplyingPlan(plan.reasoning))
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
    scope.track(TaskEvent.ApplyingToolOnStep(tool.name, step.reasoning))
    val prompt =
      """|
      |Previous knowledge:
      |$previousOutput
      |original input:
      |$input
    """
        .trimMargin()
    val output = tool(prompt)
    scope.track(Completed(step, output))
    return ToolsExecutionTrace.Completed(results = mapOf(step to output), output = output)
  }

  suspend fun createExecutionPlan(task: String): ToolsExecutionPlan {
    scope.track(TaskEvent.CreatingExecutionPlan(task))

    val messages: Prompt = Prompt {
      +system(
        "You are an expert in tool selection that can choose the best tools for a specific task based on the tools descriptions"
      )
      +assistant("Given the following task:")
      +assistant(task)
      +assistant("Given the following tools:")
      tools.forEach { +assistant("${it.name}: ${it.description}") }
      +user("Follow the next instructions")
      +user("Select the best execution plan with tools for the `task` based on the `tools`")
      +user(
        "Your `RESPONSE` MUST be a `ToolsExecutionPlan` object, where the `steps` determine how the execution plan will run the tools"
      )
      instructions.forEach { +user(it) }
    }

    return model.prompt(
      scope = scope,
      serializer = ToolsExecutionPlan.serializer(),
      prompt = messages
    )
  }
}
