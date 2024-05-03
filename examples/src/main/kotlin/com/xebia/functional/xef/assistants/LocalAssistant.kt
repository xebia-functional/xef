package com.xebia.functional.xef.assistants

import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.llm.assistants.Assistant
import com.xebia.functional.xef.llm.assistants.AssistantThread
import com.xebia.functional.xef.llm.assistants.RunDelta
import com.xebia.functional.xef.llm.assistants.Tool
import com.xebia.functional.xef.llm.assistants.local.InMemoryAssistants

suspend fun main() {

  // language=yaml
  val yamlConfig =
    """
      model: "llama3-70b-8192"
      name: "Math Assistant"
      description: "Help with math"
      instructions: Run the SumTool to add two numbers if asked otherwise reply to the user message
      tools:
        - type: "function"
          name: "SumTool"
      metadata:
        version: "1.0"
        created_by: "Xef"
        use_case: "Help with math"
        language: "English"
        additional_info: "This assistant is continuously updated with the latest information."
    """
      .trimIndent()
  val tools = listOf(Tool.toolOf(SumTool()))
  val config = Config(baseUrl = "https://api.groq.com/openai/v1/", token = "your_token")
  val chat = OpenAI(config = config, logRequests = true).chat
  val localAssistants = InMemoryAssistants(api = chat)
  val assistant =
    Assistant.fromConfig(request = yamlConfig, toolsConfig = tools, assistantsApi = localAssistants)
  val assistantInfo = assistant.get()
  println("assistant: $assistantInfo")
  val thread = AssistantThread(api = localAssistants)
  println("Enter a message or type 'exit' to quit:")
  while (true) {
    val input = readlnOrNull() ?: break
    if (input == "exit") break
    thread.createMessage(input)
    thread.run(assistant).collect {
      when (it) {
        is RunDelta.MessageDelta -> print(it.messageDelta.delta.content.firstOrNull()?.text?.value)
        else -> it.printEvent()
      }
    }
  }
}
