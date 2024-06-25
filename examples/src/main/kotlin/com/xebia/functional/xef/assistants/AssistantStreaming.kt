package com.xebia.functional.xef.assistants

import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.llm.assistants.Assistant
import com.xebia.functional.xef.llm.assistants.AssistantThread
import com.xebia.functional.xef.llm.assistants.Tool
import com.xebia.functional.xef.metrics.Metric
import io.github.nomisrev.openapi.CreateRunRequest

suspend fun main() {

  val metric = Metric.EMPTY
  // val metric = com.xebia.functional.xef.opentelemetry.OpenTelemetryMetric()

  val assistant =
    Assistant(
      assistantId = "asst_UxczzpJkysC0l424ood87DAk",
      toolsConfig = listOf(Tool.toolOf(SumTool()))
    )
  val thread = AssistantThread(threads = OpenAI().threads, metric = metric)
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
