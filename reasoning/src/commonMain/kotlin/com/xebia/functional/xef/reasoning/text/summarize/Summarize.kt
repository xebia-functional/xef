package com.xebia.functional.xef.reasoning.text.summarize

import arrow.fx.coroutines.parMap
import com.xebia.functional.tokenizer.truncateText
import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import io.github.oshai.kotlinlogging.KotlinLogging

class Summarize(
  private val model: Chat,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}

  private suspend fun summarizeChunk(query: String, chunk: String, summaryLength: Int): String {
    logger.info { "📝 Summarizing chunk: ${chunk.length} with expected words: $summaryLength" }
    val maxContextLength: Int =
      model.modelType.maxContextLength - 1000 // magic padding for functions and memory
    val promptTokens: Int = model.modelType.encoding.countTokens(chunk)
    val remainingTokens: Int = maxContextLength - promptTokens
    return model
      .promptMessage(
        ExpertSystem(
            system =
              "You are an expert information summarizer that is able to provide a summary of a long text in an exact number of words",
            query =
              """|
                |Given the following text:
                |```text
                |${model.modelType.encoding.truncateText(chunk, remainingTokens)}
                |```
                |And the following query:
                |```query
                |${query}
                |```
            """
                .trimMargin(),
            instructions =
              listOf(
                "Summarize the `text` in neutral tone that is relevant to the `query`",
                "If the `text` is relevant to the `query` Your `RESPONSE` MUST have exactly $summaryLength words",
                "If the `text` is NOT relevant to the `query` Your `RESPONSE` MUST be an empty String: ``",
                "The empty `RESPONSE` tells us the text is not relevant and you SHOULD NOT add additional info in the `RESPONSE`"
              ) + instructions
          )
          .message,
        scope.context,
        scope.conversationId
      )
      .also {
        val tokens: Int = model.modelType.encoding.countTokens(it)
        logger.info { "📝 Summarized chunk: ${it.length} with final tokens: ${tokens}" }
      }
  }

  private fun chunkText(text: String): List<String> {
    val maxTokens =
      model.modelType.maxContextLength - 2000 // magic padding for functions and memory
    val firstPart = model.modelType.encoding.truncateText(text, maxTokens)
    val remainingText = text.removePrefix(firstPart)

    return if (remainingText.isNotEmpty()) {
      listOf(firstPart) + chunkText(remainingText)
    } else {
      listOf(firstPart)
    }
  }

  tailrec suspend fun summarizeLargeText(query: String, text: String, summaryLength: Int): String {
    logger.info {
      "📚 Summarizing large text of length ${text.length} to approximately $summaryLength words"
    }
    // Split the text into chunks that are less than maxTokens

    val chunks = chunkText(text.replace("\n", " "))

    logger.info { "📚 Split text into ${chunks.size} chunks" }

    // For each chunk, get a summary in parallel
    val chunkSummaries =
      chunks.parMap { chunk ->
        val chunkSummaryLength = summaryLength / chunks.size
        summarizeChunk(query, chunk, chunkSummaryLength)
      }

    logger.info { "📚 Summarized ${chunks.size} chunks" }

    // Join the chunk summaries into one text
    val joinedSummaries = chunkSummaries.joinToString(" ")

    val joinedSummariesTokens = model.modelType.encoding.countTokens(joinedSummaries)

    // Resummarize the joined summaries if it is longer than summaryLength
    return if (joinedSummariesTokens > summaryLength) {
      summarizeLargeText(query, joinedSummaries, summaryLength)
    } else {
      joinedSummaries
    }
  }
}
