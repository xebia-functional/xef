package com.xebia.functional.xef.reasoning.pdf

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.pdf.pdf
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.reasoning.text.summarize.Summarize
import com.xebia.functional.xef.reasoning.text.summarize.SummaryLength
import com.xebia.functional.xef.reasoning.tools.Tool
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import kotlinx.serialization.Serializable

@Serializable data class ExtractedPDFFile(val absolutePath: String)

class ReadPDFFromFile
@JvmOverloads
constructor(
  private val chat: Chat,
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val summaryLength: SummaryLength = SummaryLength.DEFAULT
) : Tool {

  private val logger = KotlinLogging.logger {}

  override val name: String = "Read PDF from File"

  override val description: String = "Reads the content of a PDF as String from a File"

  override suspend fun invoke(input: String): String {
    val extracted: ExtractedPDFFile =
      model.prompt(
        prompt =
          Prompt(
            """|
        |Please provide the file that you want to read a PDF from
        |given this input:
        |
        |$input
      """
              .trimMargin()
          ),
        context = scope.context,
        conversationId = scope.conversationId,
        serializer = ExtractedPDFFile.serializer()
      )

    logger.info { "Reading pdf file ${extracted.absolutePath}" }

    val docs = pdf(File(extracted.absolutePath))
    val summary = Summarize(chat, scope, summaryLength).invoke(docs.joinToString(" "))
    return summary
  }
}
