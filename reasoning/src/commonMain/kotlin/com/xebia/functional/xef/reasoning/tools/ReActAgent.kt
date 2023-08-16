package com.xebia.functional.xef.reasoning.tools

import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.auto.Description
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable

class ReActAgent(
  private val model: ChatWithFunctions,
  private val scope: Conversation,
  private val tools: List<Tool>,
  private val maxIterations: Int = 10,
) {

  private val logger = KotlinLogging.logger {}

  private suspend fun createExecutionPlan(
    input: Prompt,
    chain: List<ThoughtObservation>
  ): AgentPlan {
    val choice: AgentChoice = agentChoice(input, chain)

    return when (choice.choice) {
      AgentChoiceType.CONTINUE -> agentAction(input, chain)
      AgentChoiceType.FINISH -> {
        agentFinish(input, chain)
      }
    }
  }

  private suspend fun agentFinish(input: Prompt, chain: List<ThoughtObservation>): AgentFinish =
    model.prompt(
      scope = scope,
      serializer = AgentFinish.serializer(),
      prompt =
        Prompt {
          +system("You are an expert in providing answers")
          +chain.chainToMessages()
          +user("Provide the final answer to the `input` in a sentence or paragraph")
          +user("input: $input")
          +assistant(
            "I should create a AgentFinish object with the final answer based on the thoughts and observations"
          )
        }
    )

  private suspend fun agentAction(input: Prompt, chain: List<ThoughtObservation>): AgentAction =
    model.prompt(
      scope = scope,
      serializer = AgentAction.serializer(),
      prompt =
        Prompt {
          +system(
            "You are an expert in tool selection. You are given a `input` and a `chain` of thoughts and observations."
          )
          +user("input:")
          +input
          +assistant("chain:")
          +chain.chainToMessages()
          +assistant("I can only use this tools:")
          +tools.toolsToMessages()
          +assistant(
            "I will not repeat the `toolInput` if the same one produced no satisfactory results in the observations"
          )
          +user("Provide the next tool to use and the `toolInput` for the tool")
        }
    )

  private fun List<Tool>.toolsToMessages(): List<Message> = flatMap {
    Prompt { +assistant("${it.name}: ${it.description}") }.messages
  }

  private fun List<ThoughtObservation>.chainToMessages(): List<Message> = flatMap {
    Prompt {
        +assistant("Thought: ${it.thought}")
        +assistant("Observation: ${it.observation}")
      }
      .messages
  }

  private suspend fun agentChoice(input: Prompt, chain: List<ThoughtObservation>): AgentChoice =
    model.prompt(
      prompt =
        Prompt {
          +input
          +assistant("chain:")
          +chain.chainToMessages()
          +assistant(
            "`CONTINUE` if the `input` has not been answered by the observations in the `chain`"
          )
          +assistant("`FINISH` if the `input` has been answered by the observations in the `chain`")
        },
      scope = scope,
      serializer = AgentChoice.serializer()
    )

  private suspend fun createInitialThought(input: Prompt): Thought {
    return model.prompt(
      prompt =
        Prompt {
          +system("You are an expert in providing next steps to solve a problem")
          +system("You are given a `input` provided by the user")
          +user("input:")
          +input
          +assistant("I have access to tools:")
          +tools.toolsToMessages()
          +assistant("I should create a Thought object with the next thought based on the `input`")
          +user("Provide the next thought based on the `input`")
        },
      scope = scope,
      serializer = Thought.serializer()
    )
  }

  private tailrec suspend fun runRec(
    input: Prompt,
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
        val observation: String? =
          tools.find { it.name.equals(plan.tool, ignoreCase = true) }?.invoke(plan.toolInput)
        if (observation == null) {
          logger.info { "🤷‍ Could not find ${plan.tool}" }
          runRec(
            input,
            chain +
              ThoughtObservation(
                plan.thought,
                "Result of running ${plan.tool}[${plan.toolInput}]: " +
                  "🤷‍ Could not find ${plan.tool}, will not try this tool again"
              ),
            currentIteration + 1
          )
        } else {
          logger.info { "👀 $observation" }
          runRec(
            input,
            chain +
              ThoughtObservation(
                plan.thought,
                "Result of running ${plan.tool}[${plan.toolInput}]: " + observation
              ),
            currentIteration + 1
          )
        }
      }
      is AgentFinish -> {
        logger.info { "✅ ${plan.finalAnswer}" }
        plan.finalAnswer
      }
    }
  }

  suspend fun run(input: Prompt): String {
    val thought = createInitialThought(input)
    logger.info { "🤔 ${thought.thought}" }
    return runRec(input, listOf(ThoughtObservation("I should get started", thought.thought)), 0)
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
