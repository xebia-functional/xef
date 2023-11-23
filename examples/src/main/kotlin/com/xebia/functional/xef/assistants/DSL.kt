package com.xebia.functional.xef.assistants

import com.xebia.functional.openai.models.*
import com.xebia.functional.xef.llm.assistants.Assistant
import com.xebia.functional.xef.llm.assistants.AssistantThread

suspend fun main() {

  //  val assistant = Assistant(
  //    name = "Math Tutor",
  //    instructions = "You are a personal math tutor. Write and run code to answer math
  // questions.",
  //    tools = listOf(
  //      AssistantObjectToolsInner(
  //        type = AssistantObjectToolsInner.Type.code_interpreter
  //      )
  //    ),
  //    model = "gpt-4-1106-preview"
  //  )
  val assistant = Assistant(assistantId = "asst_RVNViA1YfTXwv8hTfdyGGNHW")
  val thread = AssistantThread()
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

  val calls =
    step.runStep.stepDetails.toolCalls.map {
      when (it.type) {
        RunStepDetailsToolCallsObjectToolCallsInner.Type.code_interpreter -> "CodeInterpreter"
        RunStepDetailsToolCallsObjectToolCallsInner.Type.retrieval -> "Retrieval"
        RunStepDetailsToolCallsObjectToolCallsInner.Type.function ->
          "${it.function?.name}(${it.function?.arguments ?: ""}): "
      }
    }
  println(
    "${step.runStep.stepDetails.type.value} ${stepStatusEmoji(step.runStep.status)} ${calls.joinToString()} "
  )
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
  println("Assistant: ${runStatusEmoji(run)}")
}
