package com.xebia.functional.xef.reasoning.search

import com.xebia.functional.xef.agents.search
import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.reasoning.text.Text
import com.xebia.functional.xef.reasoning.text.summarize.Summarize
import com.xebia.functional.xef.reasoning.text.summarize.SummaryLength
import com.xebia.functional.xef.reasoning.tools.LLMTool
import com.xebia.functional.xef.reasoning.tools.Tool

class Search @JvmOverloads constructor(
  private val model: Chat,
  private val scope: CoreAIScope,
  private val summaryLength: SummaryLength = SummaryLength.DEFAULT,
  private val instructions: List<String> = emptyList(),
  private val keywordExtraction: LLMTool = Text(model, scope).keywordExtraction
) : Tool {
  override val name: String = "Search"

  override val description: String = "Search the web for the best answer and summarize what's found"

  override suspend fun invoke(input: String): String {
    val keywords = keywordExtraction.invoke(input)
    val docs = search(keywords)
    val combinedDocs = docs.joinToString("\n")
    return Summarize(model, scope, summaryLength, instructions).invoke(combinedDocs)
  }
}
