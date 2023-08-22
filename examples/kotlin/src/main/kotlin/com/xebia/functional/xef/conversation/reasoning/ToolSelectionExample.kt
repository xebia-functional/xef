package com.xebia.functional.xef.conversation.reasoning

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.log
import com.xebia.functional.xef.reasoning.filesystem.Files
import com.xebia.functional.xef.reasoning.pdf.PDF
import com.xebia.functional.xef.reasoning.pdf.ReadPDFTracing
import com.xebia.functional.xef.reasoning.text.Text
import com.xebia.functional.xef.reasoning.text.summarize.*
import com.xebia.functional.xef.reasoning.tools.*
import com.xebia.functional.xef.tracing.Tracker
import com.xebia.functional.xef.tracing.createDispatcher
import io.github.oshai.kotlinlogging.KotlinLogging

suspend fun main() {
  val logger = KotlinLogging.logger {}

  val tracing = Tracker<ToolSelectionTracing> {
    when (this) {
      is TaskEvent.ApplyingTool -> "🔍 Applying inferred tools for task: $task"
      is TaskEvent.ApplyingPlan -> "🔍 Applying execution plan with reasoning: $reasoning"
      is TaskEvent.ApplyingToolOnStep -> "🔍 Applying tool: $tool for step: $reasoning"
      is TaskEvent.CreatingExecutionPlan -> "🔍 Creating execution plan for task: $task"
      is Completed ->"""
        step : $step
        output : $output
      """.trimIndent()
    }.also { logger.info { it } }
  }

  val summary = Tracker<SummarizeTracing> {
    when (this) {
      is SummarizingChunk -> "📝 Summarizing chunk with prompt tokens $tokens for length $length"
      is SummarizedChunk -> "📝 Summarized chunk in tokens: $tokens"
      is SummarizingText -> "📚 Summarizing large text of tokens $tokens to approximately $length tokens"
      is SplitText -> "📚 Split text into $chunks chunks"
      is SummarizedChunks -> "📚 Summarized $count chunks"
    }.also { logger.info { it } }
  }

  val pdf = Tracker<ReadPDFTracing> {
    when (this) {
      is ReadPDFTracing.ReadingUrl -> "Reading url $url"
    }.also { logger.info { it } }
  }

  OpenAI.conversation(createDispatcher(tracing, summary, pdf, OpenAI.log, )) {
    val model = OpenAI().DEFAULT_CHAT
    val serialization = OpenAI().DEFAULT_SERIALIZATION
    val text = Text(model = model, scope = this)
    val files = Files(model = serialization, scope = this)
    val pdf = PDF(chat = model, model = serialization, scope = this)

    val toolSelection =
      ToolSelection(
        model = serialization,
        scope = this,
        tools =
        listOf(
          text.summarize,
          pdf.readPDFFromUrl,
          files.readFile,
          files.writeToTextFile,
        ),
      )

    val result =
      toolSelection.applyInferredTools(
        """|
    |Extract information from https://arxiv.org/pdf/2305.10601.pdf
  """.trimMargin()
      )
//    println(result)
  }
}
