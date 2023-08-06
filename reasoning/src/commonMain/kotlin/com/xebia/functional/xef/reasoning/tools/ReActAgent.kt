package com.xebia.functional.xef.reasoning.tools

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.auto.Description
import com.xebia.functional.xef.auto.PromptConfiguration
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

  private suspend fun createExecutionPlan(
    input: String,
    chain: List<ThoughtObservation>,
    promptConfiguration: PromptConfiguration
  ): AgentPlan {
    val choice: AgentChoice = agentChoice(promptConfiguration, input, chain)

    return when (choice.choice) {
      AgentChoiceType.CONTINUE -> agentAction(input, chain)
      AgentChoiceType.FINISH -> {
        agentFinish(input, chain)
      }
    }
  }

  private suspend fun agentFinish(input: String, chain: List<ThoughtObservation>): AgentFinish =
    model.prompt(
      context = scope.context,
      conversationId = scope.conversationId,
      serializer = AgentFinish.serializer(),
      prompt =
        ExpertSystem(
          system = "You are an expert in providing answers",
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

  private suspend fun agentAction(input: String, chain: List<ThoughtObservation>): AgentAction =
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

  private suspend fun agentChoice(
    promptConfiguration: PromptConfiguration,
    input: String,
    chain: List<ThoughtObservation>
  ): AgentChoice =
    model.prompt(
      context = scope.context,
      conversationId = scope.conversationId,
      serializer = AgentChoice.serializer(),
      promptConfiguration = promptConfiguration,
      prompt =
        ExpertSystem(
          system =
            "You will reflect on the `input` and `chain` and decide whether you are done or not",
          query =
            """|
                  |Given the following input:
                  |```input
                  |${input}
                  |```
                  |And the following chain of thoughts and observations:
                  |```chain
                  |${
        chain.joinToString("\n") { c ->
          """
                      |Thought: ${c.thought} 
                      |Observation: ${c.observation} 
                      """.trimMargin()
        }
      }
                  |```
              """
              .trimMargin(),
          instructions =
            listOf(
              "Choose `CONTINUE` if you are not 100% certain that all elements in the original `input` question are answered completely by the info found in the `chain`",
              "Choose `CONTINUE` if the `chain` needs more information to be able to completely answer all elements in the `input` question",
              "Choose `FINISH` if you are 100% certain that all elements in the `input` question are answered by the info found in the `chain` and are not a list of steps to achieve the goal.",
            )
        )
    )

  private suspend fun createInitialThought(
    input: String,
    promptConfiguration: PromptConfiguration
  ): Thought {
    logger.info { "🤔 $input" }
    return model.prompt(
      context = scope.context,
      conversationId = scope.conversationId,
      serializer = Thought.serializer(),
      promptConfiguration = promptConfiguration,
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
    currentIteration: Int,
    promptConfiguration: PromptConfiguration
  ): String {

    if (currentIteration > maxIterations) {
      logger.info { "🤷‍ Max iterations reached" }
      return "🤷‍ Max iterations reached"
    }

    val plan: AgentPlan = createExecutionPlan(input, chain, promptConfiguration)

    return when (plan) {
      is AgentAction -> {
        logger.info { "🤔 ${plan.thought}" }
        logger.info { "🛠 ${plan.tool}[${plan.toolInput}]" }
        val observation: String? = tools.find { it.name == plan.tool }?.invoke(plan.toolInput)
        if (observation == null) {
          logger.info { "🤷‍ Could not find ${plan.tool}" }
          runRec(input, chain, currentIteration + 1, promptConfiguration)
        } else {
          logger.info { "👀 $observation" }
          runRec(
            input,
            chain + ThoughtObservation(plan.thought, observation),
            currentIteration + 1,
            promptConfiguration
          )
        }
      }
      is AgentFinish -> {
        logger.info { "✅ ${plan.finalAnswer}" }
        plan.finalAnswer
      }
    }
  }

  suspend fun run(
    input: String,
    promptConfiguration: PromptConfiguration = PromptConfiguration { temperature(0.0) }
  ): String {
    val thought = createInitialThought(input, promptConfiguration)
    return runRec(input, listOf(ThoughtObservation(input, thought.thought)), 0, promptConfiguration)
  }
}

sealed class AgentPlan

@Serializable
data class AgentAction(
  @Description(["The reasoning behind the tool you are going to run"]) val thought: String,
  @Description(["The tool to execute the next step, one must be chosen from the `tools`"])
  val tool: String,
  @Description(["The input for the selected `tool`"]) val toolInput: String
) : AgentPlan()

@Serializable
data class AgentFinish(
  @Description(
    ["The final answer that satisfies ALL the `input` expressed in one text sentence or paragraph"]
  )
  val finalAnswer: String
) : AgentPlan()

@Serializable
data class AgentChoice(
  @Description(
    [
      "Choose `CONTINUE` if you want to run a tool or `FINISH` if you want to provide the final answer"
    ]
  )
  val choice: AgentChoiceType
)

enum class AgentChoiceType {
  CONTINUE,
  FINISH
}

@Serializable data class Thought(val thought: String)

data class ThoughtObservation(val thought: String, val observation: String)
