package com.xebia.functional.xef.reasoning.pdf

import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.pdf.pdf
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.reasoning.text.summarize.Summarize
import com.xebia.functional.xef.reasoning.text.summarize.SummaryLength
import com.xebia.functional.xef.reasoning.tools.Tool
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable

@Serializable data class ExtractedUrl(val url: String)

class ReadPDFFromUrl
@JvmOverloads
constructor(
  private val chat: Chat,
  private val model: ChatWithFunctions,
  private val scope: Conversation,
  private val summaryLength: SummaryLength = SummaryLength.DEFAULT
) : Tool {

  private val logger = KotlinLogging.logger {}

  override val name: String = "Read PDF from URL"

  override val description: String = "Reads the content of a PDF as String from a URL"

  override suspend fun invoke(input: String): String {
    val extracted: ExtractedUrl =
      model.prompt(
        prompt =
          Prompt(
            """|
        |Please provide the url that you want to read a PDF from
        |given this input:
        |
        |$input
      """
              .trimMargin()
          ),
        scope = scope,
        serializer = ExtractedUrl.serializer()
      )

    logger.info { "Reading url ${extracted.url}" }

    val docs = pdf(extracted.url)
    val summary = Summarize(chat, scope, summaryLength).invoke(docs.joinToString(" "))
    return summary
  }
}
