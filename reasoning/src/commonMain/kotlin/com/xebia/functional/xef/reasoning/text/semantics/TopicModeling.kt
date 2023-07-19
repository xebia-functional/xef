package com.xebia.functional.xef.reasoning.text.semantics

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class TopicModeling(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}

  suspend fun extractTopics(documents: List<String>): Topics {
    logger.info { "🔍 Extracting topics from documents: ${documents.size}" }
    return callModel<Topics>(
        model,
        scope,
        prompt =
          ExpertSystem(
            system =
              "You are an expert in topic modeling that can extract topics from a corpus of documents",
            query =
              """|
                |Given the following documents:
                |```documents
                |${documents.joinToString("\n")}
                |```
            """
                .trimMargin(),
            instructions =
              listOf(
                "Extract topics from the `documents`",
                "Your `RESPONSE` MUST be a list of `Topic` objects, where each object has the `name` and its `relevance`"
              ) + instructions
          )
      )
      
  }
}
