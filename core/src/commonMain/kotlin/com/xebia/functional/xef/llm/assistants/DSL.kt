package com.xebia.functional.xef.llm.assistants

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
  val conversation =
    Thread(
      messages = listOf(MessageWithFiles("hello!", emptyList())),
    )
  conversation.run(assistant).collect {
    when (it) {
      is Thread.RunDelta.Run -> {
        println(it.message.status.value)
      }
      is Thread.RunDelta.Step -> {
        println(it.runStep.status)
      }
      is Thread.RunDelta.ReceivedMessage -> {
        it.message.content.forEach { println(it.text.value) }
      }
    }
  }
}
