package com.xebia.functional.xef.assistants

import com.xebia.functional.openai.generated.model.CreateRunRequest
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.llm.assistants.Assistant
import com.xebia.functional.xef.llm.assistants.AssistantThread
import com.xebia.functional.xef.llm.assistants.Tool
import com.xebia.functional.xef.metrics.Metric

suspend fun main() {

  val metric = Metric.EMPTY
  // val metric = com.xebia.functional.xef.opentelemetry.OpenTelemetryMetric()

  val assistant =
    Assistant(
      assistantId = "asst_BwQvmWIbGUMDvCuXOtAFH8B6",
      toolsConfig = listOf(Tool.toolOf(SumTool()))
    )
  val config = Config(org = null)
  val api = OpenAI(config = config, logRequests = true).assistants
  val thread = AssistantThread(api = api, metric = metric)
  println("Welcome to the Math tutor, ask me anything about math:")
  val userInput = "What is 1+1, explain all the steps and tools you used to solve it."
  thread.createMessage(userInput)
  runAssistantAndDisplayResults(thread, assistant)
}

private suspend fun runAssistantAndDisplayResults(thread: AssistantThread, assistant: Assistant) {
  val assistantObject = assistant.get()
  thread.createRunStream(assistant, CreateRunRequest(assistantId = assistantObject.id)).collect {
    it.printEvent()
  }
}
