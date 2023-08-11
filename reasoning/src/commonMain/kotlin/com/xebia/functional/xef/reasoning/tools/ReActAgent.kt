package com.xebia.functional.xef.reasoning.tools

import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.auto.Description
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Message.Companion.assistantMessage
import com.xebia.functional.xef.llm.models.chat.Message.Companion.systemMessage
import com.xebia.functional.xef.llm.models.chat.Message.Companion.userMessage
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
    input: List<Message>,
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

  private suspend fun agentFinish(
    input: List<Message>,
    chain: List<ThoughtObservation>
  ): AgentFinish =
    model.prompt(
      scope = scope,
      serializer = AgentFinish.serializer(),
      messages =
        listOf(
          systemMessage { "You are an expert in providing answers" },
        ) +
          chain.chainToMessages() +
          listOf(
            userMessage { "Provide the final answer to the `input` in a sentence or paragraph" },
            userMessage { "input: $input" },
            assistantMessage {
              "I should create a AgentFinish object with the final answer based on the thoughts and observations"
            }
          )
    )

  private suspend fun agentAction(
    input: List<Message>,
    chain: List<ThoughtObservation>
  ): AgentAction =
    model.prompt(
      scope = scope,
      serializer = AgentAction.serializer(),
      messages =
        listOf(
          systemMessage {
            "You are an expert in tool selection. You are given a `input` and a `chain` of thoughts and observations."
          },
          userMessage { "input:" },
        ) +
          input +
          listOf(
            assistantMessage { "chain:" },
          ) +
          chain.chainToMessages() +
          listOf(
            assistantMessage { "I can only use this tools:" },
          ) +
          tools.toolsToMessages() +
          listOf(
            assistantMessage {
              "I will not repeat the `toolInput` if the same one produced no satisfactory results in the observations"
            },
            userMessage { "Provide the next tool to use and the `toolInput` for the tool" },
          )
    )

  private suspend fun List<Tool>.toolsToMessages(): List<Message> = flatMap {
    listOf(
      assistantMessage { "${it.name}: ${it.description}" },
    )
  }

  private suspend fun List<ThoughtObservation>.chainToMessages(): List<Message> = flatMap {
    listOf(
      assistantMessage { "Thought: ${it.thought}" },
      assistantMessage { "Observation: ${it.observation}" },
    )
  }

  private suspend fun agentChoice(
    promptConfiguration: PromptConfiguration,
    input: List<Message>,
    chain: List<ThoughtObservation>
  ): AgentChoice =
    model.prompt(
      scope = scope,
      serializer = AgentChoice.serializer(),
      promptConfiguration = promptConfiguration,
      messages =
        input +
          listOf(
            assistantMessage { "chain:" },
          ) +
          chain.chainToMessages() +
          listOf(
            assistantMessage {
              "`CONTINUE` if the `input` has not been answered by the observations in the `chain`"
            },
            assistantMessage {
              "`FINISH` if the `input` has been answered by the observations in the `chain`"
            },
          )
    )

  private suspend fun createInitialThought(
    input: List<Message>,
    promptConfiguration: PromptConfiguration
  ): Thought {
    return model.prompt(
      scope = scope,
      serializer = Thought.serializer(),
      promptConfiguration = promptConfiguration,
      messages =
        listOf(
          systemMessage { "You are an expert in providing next steps to solve a problem" },
          systemMessage { "You are given a `input` provided by the user" },
          userMessage { "input:" },
        ) +
          input +
          listOf(
            assistantMessage { "I have access to tools:" },
          ) +
          tools.toolsToMessages() +
          listOf(
            assistantMessage {
              "I should create a Thought object with the next thought based on the `input`"
            },
            userMessage { "Provide the next thought based on the `input`" },
          )
    )
  }

  private tailrec suspend fun runRec(
    input: List<Message>,
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
            currentIteration + 1,
            promptConfiguration
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
    input: List<Message>,
    promptConfiguration: PromptConfiguration = PromptConfiguration { temperature(0.0) }
  ): String {
    val thought = createInitialThought(input, promptConfiguration)
    logger.info { "🤔 ${thought.thought}" }
    return runRec(
      input,
      listOf(ThoughtObservation("I should get started", thought.thought)),
      0,
      promptConfiguration
    )
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
