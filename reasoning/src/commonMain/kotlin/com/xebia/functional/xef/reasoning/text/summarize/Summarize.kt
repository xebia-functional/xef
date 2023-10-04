package com.xebia.functional.xef.reasoning.text.summarize

import arrow.fx.coroutines.parMap
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.models.MaxContextLength
import com.xebia.functional.xef.llm.truncateText
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.assistantSteps
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.tools.Tool
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.jvm.JvmField

sealed class SummaryLength {
  class Unbound : SummaryLength()

  data class Bound(val length: Int) : SummaryLength()

  companion object {
    @JvmField val DEFAULT: SummaryLength = Bound(1000)
  }
}

class Summarize(
  private val model: Chat,
  private val scope: Conversation,
  private val summaryLength: SummaryLength = SummaryLength.DEFAULT,
  private val instructions: List<String> = emptyList(),
) : Tool {

  private val logger = KotlinLogging.logger {}

  override val name: String = "Summarize"
  override val description: String = "Summarize text"

  override suspend fun invoke(input: String): String =
    summarizeLargeText(text = input, summaryLength = summaryLength)

  private suspend fun summarizeChunk(chunk: String, summaryLength: SummaryLength): String {
    val maxContextLength = when(val contextLength = model.contextLength) {
      is MaxContextLength.Combined -> contextLength.total
      is MaxContextLength.FixIO -> contextLength.input
    } - 1000 // magic padding for functions and memory
    val promptTokens: Int = model.estimateTokens(chunk)
    logger.info {
      "📝 Summarizing chunk with prompt tokens $promptTokens for length $summaryLength"
    }
    val remainingTokens: Int = maxContextLength - promptTokens
    val truncatedChunk = model.truncateText(chunk, remainingTokens)

    val messages = Prompt {
      +system(
        "You are an expert information summarizer that is able to provide a summary of a text in less than a maximum number of words"
      )
      +user(
        """|
                  |Given the following text:
                  |```text
                  |$truncatedChunk
                  |```
              """
          .trimMargin()
      )
      +assistantSteps {
        listOf(
          "Summarize the `text` in max $summaryLength words",
          "Reply with an empty response: ` ` if the text can't be summarized"
        ) + instructions
      }
    }

    return model.promptMessage(messages, scope).also {
      val tokens: Int = model.estimateTokens(it)
      logger.info { "📝 Summarized chunk in tokens: $tokens" }
    }
  }

  private suspend fun chunkText(text: String): List<String> {
    val maxTokens = when(val contextLength = model.contextLength) {
      is MaxContextLength.Combined -> contextLength.total
      is MaxContextLength.FixIO -> contextLength.input
    } - 2000 // magic padding for functions and memory
    val firstPart = model.truncateText(text, maxTokens)
    val remainingText = text.removePrefix(firstPart)

    return if (remainingText.isNotEmpty()) {
      listOf(firstPart) + chunkText(remainingText)
    } else {
      listOf(firstPart)
    }
  }

  tailrec suspend fun summarizeLargeText(text: String, summaryLength: SummaryLength): String {
    val tokens = model.estimateTokens(text)
    logger.info {
      "📚 Summarizing large text of tokens ${tokens} to approximately $summaryLength tokens"
    }
    // Split the text into chunks that are less than maxTokens

    val chunks = chunkText(text.replace("\n", " "))

    logger.info { "📚 Split text into ${chunks.size} chunks" }

    val length =
      if (summaryLength is SummaryLength.Bound)
        SummaryLength.Bound(summaryLength.length / chunks.size)
      else SummaryLength.Unbound()

    // For each chunk, get a summary in parallel
    val chunkSummaries = chunks.parMap { chunk -> summarizeChunk(chunk, length) }

    logger.info { "📚 Summarized ${chunks.size} chunks" }

    // Join the chunk summaries into one text
    val joinedSummaries = chunkSummaries.joinToString(" ")

    val joinedSummariesTokens = model.estimateTokens(joinedSummaries)

    // Resummarize the joined summaries if it is longer than summaryLength
    return if (
      summaryLength is SummaryLength.Bound && joinedSummariesTokens > summaryLength.length
    ) {
      summarizeLargeText(joinedSummaries, summaryLength)
    } else {
      joinedSummaries
    }
  }
}
