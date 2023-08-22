package com.xebia.functional.xef.reasoning.text.summarize

import arrow.fx.coroutines.parTraverse
import com.xebia.functional.tokenizer.truncateText
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.prompt.templates.steps
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.tools.Tool
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

  override val name: String = "Summarize"
  override val description: String = "Summarize text"

  override suspend fun invoke(input: String): String =
    summarizeLargeText(text = input, summaryLength = summaryLength)

  private suspend fun summarizeChunk(chunk: String, summaryLength: SummaryLength): String {
    val maxContextLength: Int =
      model.modelType.maxContextLength - 1000 // magic padding for functions and memory
    val promptTokens: Int = model.modelType.encoding.countTokens(chunk)
    scope.track(SummarizingChunk(promptTokens,summaryLength))
    val remainingTokens: Int = maxContextLength - promptTokens

    val messages = Prompt {
      +system(
        "You are an expert information summarizer that is able to provide a summary of a text in less than a maximum number of words"
      )
      +user(
        """|
                  |Given the following text:
                  |```text
                  |${model.modelType.encoding.truncateText(chunk, remainingTokens)}
                  |```
              """
          .trimMargin()
      )
      +steps {
        (listOf(
            "Summarize the `text` in max $summaryLength words",
            "Reply with an empty response: ` ` if the text can't be summarized"
          ) + instructions)
          .forEach { +assistant(it) }
      }
    }

    return model.promptMessage(messages, scope).also {
      val tokens: Int = model.modelType.encoding.countTokens(it)
      scope.track(SummarizedChunk(tokens))
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

  tailrec suspend fun summarizeLargeText(text: String, summaryLength: SummaryLength): String {
    val tokens = model.modelType.encoding.countTokens(text)
    scope.track(SummarizingText(tokens,summaryLength))
    // Split the text into chunks that are less than maxTokens

    val chunks = chunkText(text.replace("\n", " "))

    scope.track(SplitText(chunks.size))

    val length =
      if (summaryLength is SummaryLength.Bound)
        SummaryLength.Bound(summaryLength.length / chunks.size)
      else SummaryLength.Unbound()

    // For each chunk, get a summary in parallel
    val chunkSummaries = chunks.parTraverse { chunk -> summarizeChunk(chunk, length) }

    scope.track(SummarizedChunks(chunkSummaries.size))

    // Join the chunk summaries into one text
    val joinedSummaries = chunkSummaries.joinToString(" ")

    val joinedSummariesTokens = model.modelType.encoding.countTokens(joinedSummaries)

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
