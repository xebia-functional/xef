package com.xebia.functional.xef.reasoning.text.summarize

import com.xebia.functional.xef.tracing.Event

sealed interface SummarizeTracing : Event

data class SummarizingChunk(
  val tokens: Int,
  val length: SummaryLength,
) : SummarizeTracing

data class SummarizedChunk(
  val tokens: Int
) : SummarizeTracing

data class SummarizingText(
  val tokens: Int,
  val length: SummaryLength,
) : SummarizeTracing

data class SplitText(
  val chunks: Int
) : SummarizeTracing

data class SummarizedChunks(
  val count : Int
) : SummarizeTracing