package com.xebia.functional.xef.reasoning.tools

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.auto.Description
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable

class ReActAgent(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val tools: List<Tool>
) {

  private val logger = KotlinLogging.logger {}

  // chain [thoughts and observations]
  suspend fun createExecutionPlan(input: String, chain: Map<String, String>): AgentPlan {
    logger.info { "🔍 Creating agent action for next task: $input" }
    val s: AgentPlan =
      model.prompt(
        context = scope.context,
        conversationId = scope.conversationId,
        serializer = AgentPlan.serializer(),
        prompt =
          ExpertSystem(
            system =
              "You are an expert in tool selection that can choose the next tool for a specific task based on the tools descriptions",
            query =
              """|
                |Given the following input:
                |```input
                |${input}
                |```
                |And the following tools:
                |```tools
                |${
          (tools.map {
            ToolMetadata(
              it.name,
              it.description
            )
          }).joinToString("\n") { "${it.name}: ${it.description}" }
        }
                |```
                |And the following chain of thoughts and observations:
                |```chain
                |${
          chain.map { (k, v) ->
            """
                    |Thought: $k 
                    |Observation: $v
                    """.trimMargin()
          }.joinToString("\n")
        }
                |```
            """
                .trimMargin(),
            instructions =
              listOf(
                "Select the next tool for the `input` based on the `tools` or `null` if your `thought` determines that the `input` has been properly answered",
                "Your `RESPONSE` MUST be a `AgentPlan` object, where the `thought` determine if you need to run a tool or you have achieved your `finalAnswer`",
                "If the `input` has been properly answered, then the `thought` MUST indicate that the final answer has been reached and the `finalAnswer` MUST include the final answer for the `input`",
                "If the `input` is not answered in full then the `finalAnswer` MUST be `null` and the `tool` and `toolInput` MUST be provided for the next step",
              )
          )
      )
    return s
  }

  suspend fun createInitialThought(input: String): Thought {
    logger.info { "🔍 Creating initial thought for input: $input" }
    return model.prompt(
      context = scope.context,
      conversationId = scope.conversationId,
      serializer = Thought.serializer(),
      prompt =
        ExpertSystem(
          system =
            "You are an expert in providing more descriptive inputs for tasks that a user wants to execute",
          query =
            """|
                |Given the following input:
                |```input
                |${input}
                |```
                |And the following tools:
                |```tools
                |${
          (tools.map {
            ToolMetadata(
              it.name,
              it.description
            )
          }).joinToString("\n") { "${it.name}: ${it.description}" }
        }
                |```
            """
              .trimMargin(),
          instructions =
            listOf(
              "Create a prompt that serves as 'thought' of what to do next in order to accurately describe what the user wants to do",
              "Your `RESPONSE` MUST be a `Thought` object, where the `thought` determines what the user should do next"
            )
        )
    )
  }

  private tailrec suspend fun runRec(input: String, chain: Map<String, String>): String {

    val plan: AgentPlan = createExecutionPlan(input, chain)

    logger.info { "🔍 Thought: ${plan.thought}" }

    return when {
      plan.finalAnswer != null -> {
        logger.info { "🔍 Final answer: ${plan.finalAnswer}" }
        plan.finalAnswer
      }
      plan.tool != null && plan.toolInput != null -> {
        logger.info { "🔍 Tool selected: ${plan.tool} with input: ${plan.toolInput}" }
        val observation: String? = tools.find { it.name == plan.tool }?.invoke(plan.toolInput)
        logger.info { "🔍 Observation: $observation" }
        runRec(input, chain + (plan.thought to observation.orEmpty()))
      }
      else -> {
        runRec(input, chain)
      }
    }
  }

  suspend fun run(input: String): String {
    val thought = createInitialThought(input)
    return runRec(input, mapOf(input to thought.thought))
  }
}

@Serializable
data class AgentPlan(
  @Description(
    [
      "Required. The reasoning behind the tool you are going to run or a conclusion indicating that you have reached your `finalAnswer`"
    ]
  )
  val thought: String,
  @Description(
    ["The tool to execute the next step, `null` in the case you have reached your `finalAnswer`"]
  )
  val tool: String? = null,
  @Description(
    ["The input for the selected `tool` in the case you have not reached your `finalAnswer`"]
  )
  val toolInput: String? = null,
  @Description(["The final answer that satisfies all concerns about the original `input`"])
  val finalAnswer: String? = null
)

@Serializable data class Thought(val thought: String)
