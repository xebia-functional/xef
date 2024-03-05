package com.xebia.functional.xef.assistants

import com.xebia.functional.openai.models.*
import com.xebia.functional.openai.models.ext.assistant.RunStepDetailsMessageCreationObject
import com.xebia.functional.openai.models.ext.assistant.RunStepDetailsToolCallsObject
import com.xebia.functional.xef.llm.assistants.Assistant
import com.xebia.functional.xef.llm.assistants.AssistantThread
import com.xebia.functional.xef.llm.assistants.Tool
import com.xebia.functional.xef.metrics.Metric
import kotlinx.serialization.Serializable

@Serializable data class SumInput(val left: Int, val right: Int)

class SumTool : Tool<SumInput, Int> {
  override suspend fun invoke(input: SumInput): Int {
    return input.left + input.right
  }
}

suspend fun main() {

  //  val assistant2 = Assistant(
  //    name = "Math Tutor",
  //    instructions = "You help the user with all kinds of math problems.",
  //    tools = listOf(
  //      AssistantObjectToolsInner(
  //        type = AssistantObjectToolsInner.Type.code_interpreter
  //      ),
  //      AssistantObjectToolsInner(
  //        type = AssistantObjectToolsInner.Type.retrieval,
  //      ),
  //      AssistantObjectToolsInner(
  //        type = AssistantObjectToolsInner.Type.function,
  //        function = toolOf<SumTool>()
  //      )
  //    ),
  //    model = "gpt-4-1106-preview"
  //  )
  // println("generated assistant: ${assistant2.assistantId}")

  // This example contemplate the case of using OpenTelemetry for metrics
  // To run the example with OpenTelemetry, you can execute the following commands:
  //  - # cd server/docker/opentelemetry
  //  - # docker-compose up

  val metric = Metric.EMPTY
  // val metric = com.xebia.functional.xef.opentelemetry.OpenTelemetryMetric()

  val assistant =
    Assistant(
      assistantId = "asst_UxczzpJkysC0l424ood87DAk",
      toolsConfig = listOf(Tool.toolOf(SumTool()))
    )
  val thread = AssistantThread(metric = metric)
  println("Welcome to the Math tutor, ask me anything about math:")
  while (true) {
    println()
    val userInput = readln()
    thread.createMessage(userInput)
    runAssistantAndDisplayResults(thread, assistant)
  }
}

private suspend fun runAssistantAndDisplayResults(thread: AssistantThread, assistant: Assistant) {
  val assistantObject = assistant.get()
  thread.run(assistant).collect {
    when (it) {
      is AssistantThread.RunDelta.Run -> displayRunStatus(it)
      is AssistantThread.RunDelta.Step -> displayStepsStatus(it)
      is AssistantThread.RunDelta.ReceivedMessage -> displayReceivedMessages(assistantObject, it)
    }
  }
}

private fun displayReceivedMessages(
  assistant: AssistantObject,
  receivedMessage: AssistantThread.RunDelta.ReceivedMessage
) {
  if (receivedMessage.message.role == MessageObject.Role.assistant) {
    receivedMessage.message.content.forEach {
      val text = it.text?.value
      if (text != null) {
        println("${assistant.name}: $text")
      }
      val imageFile = it.imageFile
      if (imageFile != null) {
        println("${assistant.name}: https://platform.openai.com/files/${imageFile.fileId}")
      }
    }
  }
}

private fun displayStepsStatus(step: AssistantThread.RunDelta.Step) {

  val details = step.runStep.stepDetails
  val type =
    when (details) {
      is RunStepDetailsMessageCreationObject -> details.type.value
      is RunStepDetailsToolCallsObject -> details.type.value
    }
  val calls =
    when (details) {
      is RunStepDetailsMessageCreationObject -> listOf()
      is RunStepDetailsToolCallsObject ->
        details.toolCalls.map {
          when (it.type) {
            RunStepDetailsToolCallsObjectToolCallsInner.Type.code_interpreter -> "CodeInterpreter"
            RunStepDetailsToolCallsObjectToolCallsInner.Type.retrieval -> "Retrieval"
            RunStepDetailsToolCallsObjectToolCallsInner.Type.function ->
              "${it.function?.name}(${it.function?.arguments ?: ""}) = ${it.function?.output ?: "empty"}: "
          }
        }
    }
  println("$type ${stepStatusEmoji(step.runStep.status)} ${calls.joinToString()} ")
}

private fun runStatusEmoji(run: AssistantThread.RunDelta.Run) =
  when (run.message.status) {
    RunObject.Status.queued -> "🕒" // Hourglass Not Done
    RunObject.Status.in_progress -> "🔄" // Clockwise Vertical Arrows
    RunObject.Status.requires_action -> "💡" // Light Bulb
    RunObject.Status.cancelling -> "🛑" // Stop Sign
    RunObject.Status.cancelled -> "❌" // Cross Mark
    RunObject.Status.failed -> "🔥" // Fire
    RunObject.Status.completed -> "🎉" // Party Popper
    RunObject.Status.expired -> "🕰️" // Mantelpiece Clock
  }

private fun stepStatusEmoji(status: RunStepObject.Status) =
  when (status) {
    RunStepObject.Status.in_progress -> "🔄" // Clockwise Vertical Arrows
    RunStepObject.Status.cancelled -> "❌" // Cross Mark
    RunStepObject.Status.failed -> "🔥" // Fire
    RunStepObject.Status.completed -> "🎉" // Party Popper
    RunStepObject.Status.expired -> "🕰️" // Mantelpiece Clock
  }

private fun displayRunStatus(run: AssistantThread.RunDelta.Run) {
  println("Assistant: ${runStatusEmoji(run)} - ${run.message.status}")
}
