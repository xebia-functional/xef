package com.xebia.functional.xef.reasoning.text.semantics

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class SemanticSimilarity(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}

  suspend fun evaluateSimilarity(text1: String, text2: String): SimilarityResult {
    logger.info { "🔍 Evaluating semantic similarity between two texts" }
    return callModel<SimilarityResult>(
        model,
        scope,
        prompt =
          ExpertSystem(
            system =
              "You are an expert in semantic similarity that can evaluate the similarity in meaning between two pieces of text",
            query =
              """|
                |Given the following texts:
                |```text1
                |${text1}
                |```
                |```text2
                |${text2}
                |```
            """
                .trimMargin(),
            instructions =
              listOf(
                "Evaluate the semantic similarity between `text1` and `text2`",
                "Your `RESPONSE` MUST be a number between 0 (completely dissimilar) and 1 (completely similar)"
              ) + instructions
          )
      )
      
  }
}
