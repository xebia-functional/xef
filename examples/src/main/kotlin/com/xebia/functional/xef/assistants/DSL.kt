package com.xebia.functional.xef.assistants

import com.xebia.functional.openai.models.RunStepDetailsToolCallsObjectToolCallsInner
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

private suspend fun runAssistantAndDisplayResults(
  thread: AssistantThread,
  assistant: Assistant
) {
  thread.run(assistant).collect {
    when (it) {
      is AssistantThread.RunDelta.Run -> displayRunStatus(it)
      is AssistantThread.RunDelta.Step -> displayStepsStatus(it)
      is AssistantThread.RunDelta.ReceivedMessage -> displayReceivedMessages(it)
    }
  }
}

private fun displayReceivedMessages(it: AssistantThread.RunDelta.ReceivedMessage) {
  it.message.content.forEach {
    it.text?.value?.let(::println)
    it.imageFile?.let(::println)
  }
}

private fun displayStepsStatus(it: AssistantThread.RunDelta.Step) {
  val type = it.runStep.stepDetails.type
  val messageId = it.runStep.stepDetails.messageCreation?.messageId
  val toolsCalls = it.runStep.stepDetails.toolCalls.map {
    when (it.type) {
      RunStepDetailsToolCallsObjectToolCallsInner.Type.code_interpreter ->
        """|
                 |Code Interpreter:
                 | input: ${it.codeInterpreter?.input}
                 | outputs: ${
          it.codeInterpreter?.outputs.orEmpty().joinToString {
            """|
                 |     |output:
                 |     |  value: ${it.logs}
                 |     |  type: ${it.image}
                 |     |""".trimMargin()
          }
        }
                 |""".trimMargin()

      RunStepDetailsToolCallsObjectToolCallsInner.Type.retrieval ->
        "Retrieval: ${it.retrieval}"

      RunStepDetailsToolCallsObjectToolCallsInner.Type.function ->
        "Function: ${it.function}, output: ${it.function?.output}"
    }
  }
  println(
    """
          |Step:
          |  type: $type
          |  messageId: $messageId
          |  toolCalls: ${toolsCalls.joinToString("\n")}
          |""".trimMargin()
  )
}

private fun displayRunStatus(it: AssistantThread.RunDelta.Run) {
  println(it.message.status.value)
}
