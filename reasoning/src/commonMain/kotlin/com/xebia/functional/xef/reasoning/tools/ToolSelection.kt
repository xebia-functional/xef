package com.xebia.functional.xef.reasoning.tools

import arrow.fx.coroutines.parMap
import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class ToolSelection(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val tools: List<Tool<*>>,
  val functions: Map<ToolMetadata, suspend (input: String) -> Tool.Out<*>> =
    tools
      .flatMap { tool -> tool.functions.map { function -> function.key to function.value } }
      .toMap(),
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}
//
//  TODO apply selection over tools but allowing tool
//  chains with multiple tools that pipe the inputs/outputs automatically to solve a problem

  suspend fun applyInferredTools(task: String): ToolsExecutionTrace {
    logger.info { "🔍 Applying inferred tools for task: $task" }
    val plan = createExecutionPlan(task)
    logger.info { "🔍 Applying execution plan with reasoning: ${plan.reasoning}" }
    // partition steps between independent and dependent steps
    val (parallel, sequential) = plan.steps.partition { step -> step.executionType == ExecutionType.Parallel }
    // apply parallel steps
    val parExecuted: Map<ToolExecutionStep, Tool.Out<*>> = parallel.parMap {
      logger.info { "🔍 Applying parallel step: $it" }
      it to (functions[it.tool]?.invoke(task) ?: Tool.Out.empty<Any?>())
    }.toMap()
    // apply sequential steps
    // the previous state is calculated by the previous step only if there is a previous step
    // otherwise the input is the task
    val seqExecuted: Map<ToolExecutionStep, Tool.Out<*>> =
      sequential.foldIndexed(emptyMap()) { index, acc, step ->
        logger.info { "🔍 Applying sequential step: ${step.reasoning}" }
        val previousStep = sequential.getOrNull(index - 1)
        val previous = if (previousStep != null && index > 0) acc[previousStep] else Tool.Out.empty<Any?>()
        acc + (step to (previous?.let {
          previousStep?.let { s ->
            functions[step.tool]?.invoke(
              it.toolOutput(s.tool).toOutputString()
            )
          }
        } ?: functions[step.tool]?.invoke(
          task
        ) ?: Tool.Out.empty()))
      }

    val executions = parExecuted + seqExecuted

    return if (executions.isEmpty()) ToolsExecutionTrace.EMPTY
    else ToolsExecutionTrace(
      executions,
      executions.values.last()
    )

  }

  suspend fun createExecutionPlan(task: String): ToolsExecutionPlan {
    logger.info { "🔍 Creating execution plan for task: $task" }
    return callModel<ToolsExecutionPlan>(
      model,
      scope,
      prompt =
      ExpertSystem(
        system =
        "You are an expert in tool selection that can choose the best tools for a specific task based on the tools descriptions",
        query =
        """|
                |Given the following task:
                |```task
                |${task}
                |```
                |And the following tools:
                |```tools
                |${functions.keys.joinToString("\n") { "${it.name}: ${it.description}" }}
                |```
            """
          .trimMargin(),
        instructions =
        listOf(
          "Select the best execution plan with tools for the `task` based on the `tools`",
          "Your `RESPONSE` MUST be a `ToolsExecutionPlan` object, where the `steps` determine how the execution plan will run the tools"
        ) + instructions
      )
    )
      
  }

  suspend inline fun <reified A> applyFunction(task: String): A? =
    selectTool(task).let { toolSelectionResult ->
      functions[toolSelectionResult.toolMetadata]?.invoke(task)?.let {
        val output = it.toolOutput(toolSelectionResult.toolMetadata)
        output.valueOrNull<A>()
      }
    }

  suspend fun selectTool(task: String): ToolSelectionResult {
    logger.info { "🔍 Selecting tool for task: $task" }
    return callModel<ToolSelectionResult>(
      model,
      scope,
      prompt =
      ExpertSystem(
        system =
        "You are an expert in tool selection that can choose the best tool for a specific task based on the tool descriptions",
        query =
        """|
                |Given the following task:
                |```task
                |${task}
                |```
                |And the following tools:
                |```tools
                |${functions.keys.joinToString("\n") { "${it.name}: ${it.description}" }}
                |```
            """
          .trimMargin(),
        instructions =
        listOf(
          "Select the best tool for the `task` based on the `tools`",
          "Your `RESPONSE` MUST be a `ToolSelectionResult` object, where the `tool` is the selected tool"
        ) + instructions
      )
    )
      
  }
}
