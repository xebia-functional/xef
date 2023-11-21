package com.xebia.functional.xef.reasoning.tools

import ai.xef.openai.OpenAIModel
import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.llm.*
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.tools.ReActAgent.CritiqueOutcome.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class ReActAgent(
  private val model: OpenAIModel<CreateChatCompletionRequestModel>,
  private val scope: Conversation,
  private val tools: List<Tool>,
  private val maxIterations: Int = 10,
  private val chatApi: ChatApi = fromEnvironment(::ChatApi),
  private val configuration: PromptConfiguration = PromptConfiguration(temperature = 0.0),
  private val critique: suspend ReActAgent.(String, Finish) -> Critique =
    { input: String, finish: Finish ->
      critiqueCall(input, finish)
    },
  private val decide: suspend ReActAgent.(String, Int, ThoughtObservation) -> Decide =
    { input: String, iterations: Int, thought: ThoughtObservation ->
      decideCall(input, iterations, thought)
    },
  private val finish: suspend ReActAgent.(String) -> Finish = { input: String ->
    finishCall(input)
  },
  private val runTool: suspend ReActAgent.() -> RunTool = { runToolCall() },
) {

  sealed class Result {
    data class MaxIterationsReached(val message: String) : Result()

    data class Log(val message: String) : Result()

    data class ToolResult(val tool: String, val input: String, val result: String) : Result()

    data class Finish(val result: String) : Result()
  }

  data class ThoughtObservation(val thought: String, val observation: String)

  @Serializable
  enum class NextStep {
    RunTool,
    Finish
  }

  @Description("Decide what to do next")
  @Serializable
  data class Decide(
    @Description(
      "`Finish` if we have all the info needed to answer the user request, `RunTool` if we need more information to provide a factual answer"
    )
    val nextStep: NextStep,
    @Description("The thought that lead to the decision") val thought: String
  )

  @Description("Provide the final answer for the user request")
  @Serializable
  data class Finish(
    @Description("The final answer for the user request which completes the conversation")
    val result: String,
    @Description("The reason why this answer covers all aspects of the user request")
    val thought: String
  )

  @Description("Run a tool to get information from the Available Tools")
  @Serializable
  data class RunTool(
    @Description("The name of the tool to run") val tool: String,
    @Description("The input for the tool") val input: String,
    @Description("The thought that lead to the decision to run this tool") val thought: String
  )

  @Serializable
  enum class CritiqueOutcome {
    CompleteAnswerForUserRequest,
    IncompleteAnswerForUserRequest
  }

  @Description(
    "Critiques the final answer to ensure it answers the user original request completely"
  )
  @Serializable
  data class Critique(
    @Description(
      "`CompleteAnswerForUserRequest` if the answer is valid and answers the user original request completely; IncompleteAnswerForUserRequest if we need more info"
    )
    val outcome: CritiqueOutcome,
    @Description(
      "The thought that lead to the critique onwhy the final answer completely answer the user request or not"
    )
    val thought: String
  )

  private suspend fun critiqueCall(prompt: String, finish: Finish): Critique {
    return chatApi.prompt(
      serializer = Critique.serializer(),
      prompt =
        Prompt(model) {
            +user(
              "If your answer is valid, reply with `CompleteAnswerForUserRequest`, otherwise `IncompleteAnswerForUserRequest` so we can gather to answer all aspects of the my user request"
            )
            +user(
              "Always reply with `IncompleteAnswerForUserRequest` if the user request has unanswered questions or parts"
            )
            +user("user request: $prompt")
            +assistant("answer: ${finish.result}")
          }
          .copy(configuration = configuration),
      scope = scope,
    )
  }

  private suspend fun decideCall(
    prompt: String,
    iterations: Int,
    thought: ThoughtObservation
  ): Decide =
    chatApi.prompt(
      serializer = Decide.serializer(),
      prompt =
        Prompt(model) {
            if (iterations == 0) {
              +system("Available Tools:")
              tools.forEach { +system("- ${it.name}: ${it.description}") }
              +user(prompt)
            }
            +assistant("thought: ${thought.thought}")
            +assistant("observation: ${thought.observation}")
          }
          .copy(configuration = configuration),
      scope = scope,
    )

  private suspend fun runToolCall(): RunTool =
    chatApi.prompt(
      serializer = RunTool.serializer(),
      prompt =
        Prompt(model) {
            +assistant(
              "I will run the tool that I think is best suited to get information in order to answer the user request"
            )
          }
          .copy(configuration = configuration),
      scope = scope,
    )

  private suspend fun finishCall(prompt: String): Finish =
    chatApi.prompt(
      serializer = Finish.serializer(),
      prompt =
        Prompt(model) {
            +user(prompt)
            +assistant("I will finish with the `result` and `thought`")
          }
          .copy(configuration = configuration),
      scope = scope,
    )

  private tailrec suspend fun FlowCollector<Result>.runRec(
    prompt: String,
    thought: ThoughtObservation,
    currentIteration: Int
  ): Unit =
    if (currentIteration > maxIterations) {
      emit(Result.MaxIterationsReached("🤷‍ Max iterations reached"))
    } else {
      val decide = decide(prompt, currentIteration, thought)
      emit(Result.Log("🤖 I decided : ${decide.thought}"))
      when (decide.nextStep) {
        NextStep.RunTool -> {
          val runTool = runTool()
          val tool = tools.find { it.name.equals(runTool.tool, ignoreCase = true) }
          if (tool == null) {
            emit(Result.Log("🤖 I don't know how to use the tool ${runTool.tool}"))
            runRec(
              prompt = prompt,
              thought =
                ThoughtObservation("${runTool.tool} not found", "I won't use this tool again"),
              currentIteration = currentIteration + 1
            )
          } else {
            emit(Result.Log("🤖 ${tool.name}[${runTool.input}]"))
            val toolResult = tool(input = runTool.input)
            emit(Result.ToolResult(tool = tool.name, input = runTool.input, result = toolResult))
            runRec(
              prompt = prompt,
              thought = ThoughtObservation("${tool.name}[${runTool.input}]", toolResult),
              currentIteration = currentIteration + 1
            )
          }
        }
        NextStep.Finish -> {
          val result = finish(prompt)
          val critique = critique(prompt, result)
          emit(Result.Log("🤖 After critiquing the answer I decided : ${critique.thought}"))
          when (critique.outcome) {
            CompleteAnswerForUserRequest -> emit(Result.Finish(result.result))
            IncompleteAnswerForUserRequest -> {
              emit(Result.Log("🤖 I need more information to answer the user request"))
              runRec(
                prompt = prompt,
                thought =
                  ThoughtObservation(
                    "I need more information",
                    "I will run another tool to get more information"
                  ),
                currentIteration = currentIteration + 1
              )
            }
          }
        }
      }
    }

  fun run(prompt: String): Flow<Result> = flow {
    emit(Result.Log("🤖 Solving... $prompt"))
    runRec(
      prompt = prompt,
      thought =
        ThoughtObservation(
          "I should get started",
          "I need to use one of the tools from the Available tools to solve the user request"
        ),
      currentIteration = 0
    )
  }
}
