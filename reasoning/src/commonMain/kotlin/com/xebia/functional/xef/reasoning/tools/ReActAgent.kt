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
  private val tools: List<Tool>,
  private val maxIterations: Int = 10,
) {

  private val logger = KotlinLogging.logger {}

  private suspend fun createExecutionPlan(input: String, chain: List<ThoughtObservation>): AgentPlan {
    val choice: AgentChoice = model.prompt(
      context = scope.context,
      conversationId = scope.conversationId,
      serializer = AgentChoice.serializer(),
      prompt =
        ExpertSystem(
          system =
            "You are an expert in making choices.",
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
              "If the `input` has been answered, then you must choose `FINISH`",
              "If the `input` is not answered in full and we could use some of the `tools` to solve it then you must choose `CONTINUE`"
            )
        )
    )

    return when (choice.choice) {
      AgentChoiceType.CONTINUE -> {
        model.prompt(
          context = scope.context,
          conversationId = scope.conversationId,
          serializer = AgentAction.serializer(),
          prompt =
          ExpertSystem(
            system =
            "You are an expert in tool selection. You are given a `input` and a `chain` of thoughts and observations.",
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
              "The `tool` and `toolInput` MUST be provided for the next step",
            )
          )
        )
      }
      AgentChoiceType.FINISH -> {
        model.prompt(
          context = scope.context,
          conversationId = scope.conversationId,
          serializer = AgentFinish.serializer(),
          prompt =
          ExpertSystem(
            system =
            "You are an expert in providing answers",
            query =
            """|
                |Given the following input:
                |```input
                |${input}
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
              "Provide the final answer to the `input` in a sentence or paragraph",
            )
          )
        )
      }
    }
  }

  private suspend fun createInitialThought(input: String): Thought {
    logger.info { "🤔 $input" }
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

  private tailrec suspend fun runRec(
    input: String,
    chain: List<ThoughtObservation>,
    currentIteration: Int
  ): String {

    if (currentIteration > maxIterations) {
      logger.info { "🤷‍ Max iterations reached" }
      return "🤷‍ Max iterations reached"
    }

    val plan: AgentPlan = createExecutionPlan(input, chain)

    return when (plan) {
      is AgentAction -> {
        logger.info { "🤔 ${plan.thought}" }
        logger.info { "🛠 ${plan.tool}[${plan.toolInput}]" }
        val observation: String? = tools.find { it.name == plan.tool }?.invoke(plan.toolInput)
        if (observation == null) {
          logger.info { "🤷‍ Could not find ${plan.tool}" }
          runRec(input, chain, currentIteration + 1)
        } else {
          logger.info { "👀 $observation" }
          runRec(input, chain + ThoughtObservation(plan.thought, observation), currentIteration + 1)
        }
      }
      is AgentFinish -> {
        logger.info { "✅ ${plan.finalAnswer}" }
        plan.finalAnswer
      }
    }
  }

  suspend fun run(input: String): String {
    val thought = createInitialThought(input)
    return runRec(input, listOf(ThoughtObservation(input, thought.thought)), 0)
  }
}

sealed class AgentPlan

@Serializable
data class AgentAction(
  @Description(["The reasoning behind the tool you are going to run"])
  val thought: String,
  @Description(["The tool to execute the next step, one must be chosen from the `tools`"])
  val tool: String,
  @Description(["The input for the selected `tool`"])
  val toolInput: String
) : AgentPlan()

@Serializable
data class AgentFinish(
  @Description(["The final answer that satisfies ALL the `input` expressed in one text sentence or paragraph"])
  val finalAnswer: String
) : AgentPlan()

@Serializable
data class AgentChoice(
  @Description(["The choice of the agent to either `CONTINUE` or `FINISH`"])
  val choice: AgentChoiceType
)

enum class AgentChoiceType {
  CONTINUE,
  FINISH
}

@Serializable
data class Thought(val thought: String)

data class ThoughtObservation(val thought: String, val observation: String)
