package com.xebia.functional.xef.assistants

import com.xebia.functional.xef.llm.assistants.Assistant
import com.xebia.functional.xef.llm.assistants.AssistantThread
import com.xebia.functional.xef.llm.assistants.MessageWithFiles
import com.xebia.functional.xef.llm.assistants.Tool

suspend fun main() {
  //  val filesApi = fromEnvironment(::FilesApi)
  //  // should only be created once and then referenced by id
  //  val file =
  //    filesApi
  //      .createFile(
  //        UploadFile("test.txt") { append("Hello World!") },
  //        FilesApi.PurposeCreateFile.assistants
  //      )
  //      .body()
  val fileId = "file-q77cZu6e6sC2TsYbUs8UX5Dj"
  // remove assistant id to create a new one
  // language=yaml
  val yamlConfig =
    """
      assistant_id: "asst_ImCKap37lLHBqV1awa0kzjZ3"
      model: "gpt-4-1106-preview"
      name: "My Custom Test Assistant"
      description: "A versatile AI assistant capable of conversational and informational tasks."
      instructions: "This assistant is designed to provide informative and engaging conversations, answer queries, and execute code when necessary."
      tools:
        - type: "code_interpreter"
        - type: "retrieval"
        - type: "function"
          name: "SumTool"
      file_ids:
        - "$fileId"
      metadata:
        version: "1.0"
        created_by: "OpenAI"
        use_case: "Customer support"
        language: "English"
        additional_info: "This assistant is continuously updated with the latest information."
    """
      .trimIndent()
  val tools = listOf(Tool.toolOf(SumTool()))
  val assistant = Assistant.fromConfig(request = yamlConfig, toolsConfig = tools)
  val assistantInfo = assistant.get()
  println("assistant: $assistantInfo")
  val thread = AssistantThread()
  thread.createMessage(MessageWithFiles("What does this file say?", listOf(fileId)))
  val stream = thread.run(assistant)
  stream.collect {
    when (it) {
      is AssistantThread.RunDelta.ReceivedMessage ->
        println("received message: ${it.message.content.firstOrNull()?.text}")
      is AssistantThread.RunDelta.Run -> println("run: ${it.message.status.value}")
      is AssistantThread.RunDelta.Step -> println("step: ${it.runStep.type.value}")
    }
  }
}
