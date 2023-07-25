package com.xebia.functional.xef.auto.reasoning

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.OpenAIEmbeddings
import com.xebia.functional.xef.reasoning.filesystem.Files
import com.xebia.functional.xef.reasoning.pdf.PDF
import com.xebia.functional.xef.reasoning.text.Text
import com.xebia.functional.xef.reasoning.tools.ToolSelection

suspend fun main() {
  val scope = CoreAIScope(OpenAIEmbeddings(OpenAI.DEFAULT_EMBEDDING))
  val model = OpenAI.DEFAULT_CHAT
  val serialization = OpenAI.DEFAULT_SERIALIZATION
  val text = Text(model = model, scope = scope)
  val files = Files(model = serialization, scope = scope)
  val pdf = PDF(chat = model, model = serialization, scope = scope)

  val toolSelection = ToolSelection(
    model = serialization,
    scope = scope,
    tools = listOf(
      text.summarize,
      pdf.readPDFFromUrl,
      files.readFile,
      files.writeToTextFile,
    ),
  )

  val result = toolSelection.applyInferredTools("""|
    |Extract information from https://arxiv.org/pdf/2305.10601.pdf
  """.trimMargin())
  println(result)
}
