package com.xebia.functional.xef.assistants

import com.xebia.functional.openai.models.ext.assistant.AssistantToolsCode
import com.xebia.functional.openai.models.ext.assistant.AssistantToolsFunction
import com.xebia.functional.openai.models.ext.assistant.AssistantToolsRetrieval
import com.xebia.functional.xef.assistants.model.Datasets
import com.xebia.functional.xef.assistants.tools.DataGovDataSetSearchTool
import com.xebia.functional.xef.assistants.tools.DownloadFilesTool
import com.xebia.functional.xef.assistants.tools.Files
import com.xebia.functional.xef.llm.assistants.Assistant
import com.xebia.functional.xef.llm.assistants.AssistantThread
import com.xebia.functional.xef.llm.assistants.Tool

suspend fun main() {

  val datasetsTool = Tool<DataGovDataSetSearchTool, Datasets>()
  val downloadFilesTool = Tool<DownloadFilesTool, Files>()

//  val datagov = Assistant(
//    name = "data.gov assistant",
//    instructions = """|
//      |Roleplay as a data.gov assistant.
//      |Instructions:
//      |1. Use the DataGovDataSetSearchTool to search for datasets on data.gov that may be relevant to what the user is asking.
//      |2. Select the relevant datasets from the DataGovDataSetSearchTool results.
//      |3. Use the DownloadFilesTool to download and load the datasets to your files.
//      |5. Whenever the user question can be displayed as a graph or chart plot the data using the CodeInterpreter tool.
//      |6. Stay polite and helpful.
//    """.trimMargin(),
//    tools = listOf(
//      AssistantToolsCode(),
//      AssistantToolsRetrieval(),
//      AssistantToolsFunction(function = datasetsTool),
//      AssistantToolsFunction(function = downloadFilesTool)
//    ),
//    model = "gpt-4-1106-preview"
//  )
//  println("generated assistant: ${datagov.assistantId}")
  val assistant = Assistant(assistantId = "asst_3LT7uWV6nSs5rsXQnHKptG6E")
  val thread = AssistantThread()
  println("🤖 ${assistant.get().name} ready!")
  while (true) {
    println()
    val userInput = readln()
    thread.createMessage(userInput)
    runAssistantAndDisplayResults(thread, assistant)
  }
}
