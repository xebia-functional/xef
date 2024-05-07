package com.xebia.functional.xef.assistants

import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.llm.assistants.Assistant
import com.xebia.functional.xef.llm.assistants.AssistantThread
import com.xebia.functional.xef.llm.assistants.RunDelta
import com.xebia.functional.xef.llm.assistants.Tool
import com.xebia.functional.xef.llm.assistants.local.InMemoryAssistants
import com.xebia.functional.xef.prompt.ToolCallStrategy

suspend fun main() {

  // language=yaml
  val yamlConfig =
    """
      model: "llama3:8b"
      name: "Math Assistant"
      description: "Help with math"
      instructions: 
        Roleplay:  Assistant that helps with math or other general questions. 
        Instructions: 
          - For math it has a SumTool. For other questions just reply with the answer.
          - If the user input does not contain information to fill the parameters of the tool,
          - the assistant will ask for the missing information.
      tools:
        - type: "function"
          name: "SumTool"
      metadata:
        ${ToolCallStrategy.Key}: ${ToolCallStrategy.InferJsonFromStringResponse.name}
    """
      .trimIndent()
  val tools = listOf(Tool.toolOf(SumTool()))
  val config = Config(baseUrl = "http://localhost:11434/v1/")
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
