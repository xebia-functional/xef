package com.xebia.functional.xef.assistants

import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.llm.assistants.Assistant
import com.xebia.functional.xef.llm.assistants.AssistantThread
import com.xebia.functional.xef.llm.assistants.RunDelta
import com.xebia.functional.xef.llm.assistants.Tool
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

  val metric = com.xebia.functional.xef.metrics.Metric.EMPTY
  //    val metric = com.xebia.functional.xef.opentelemetry.OpenTelemetryMetric()
  //  val metric = com.xebia.functional.xef.metrics.LogsMetric()
  val questionsCounter = metric.createCounter("questions-counter")

  val assistant =
    Assistant(
      assistantId = "asst_UxczzpJkysC0l424ood87DAk",
      toolsConfig = listOf(Tool.toolOf(SumTool()))
    )
  val thread = AssistantThread(api = OpenAI(logRequests = false).assistants, metric = metric)
  println("Welcome to the Math tutor, ask me anything about math:")
  while (true) {
    println()
    val userInput = readln()
    questionsCounter?.increment(1)
    thread.createMessage(userInput)
    runAssistantAndDisplayResults(thread, assistant)
  }
}

private suspend fun runAssistantAndDisplayResults(thread: AssistantThread, assistant: Assistant) {
  thread.run(assistant).collect(RunDelta::printEvent)
}
