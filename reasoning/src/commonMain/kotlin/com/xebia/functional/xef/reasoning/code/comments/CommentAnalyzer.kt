package com.xebia.functional.xef.reasoning.code.comments

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class CommentAnalyzer(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}

  suspend fun analyzeComments(sourceCode: String): CommentAnalysisResult {
    logger.info { "🔍 Analyzing code comments" }
    return callModel<CommentAnalysisResult>(
        model,
        scope,
        ExpertSystem(
          system =
            "You are an expert in code comment analysis that can analyze comments and provide feedback on their quality, completeness, and usefulness",
          query =
            """|
                |Given the following source code:
                |```code
                |${sourceCode}
                |```
            """
              .trimMargin(),
          instructions =
            listOf(
              "Analyze the comments within the `sourceCode`",
              "Provide feedback on the quality, completeness, and usefulness of each comment",
              "Your `RESPONSE` MUST be a list of `CommentAnalysis` objects, where each object contains the `comment`, `quality`, `completeness`, and `usefulness`"
            ) + instructions
        )
      )
      .also { logger.info { "🔍 Comment analysis result: $it" } }
  }
}
