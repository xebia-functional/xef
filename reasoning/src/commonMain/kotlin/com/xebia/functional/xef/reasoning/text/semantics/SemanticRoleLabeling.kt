package com.xebia.functional.xef.reasoning.text.semantics

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class SemanticRoleLabeling(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}

  suspend fun labelRoles(sentence: String): SemanticRoles {
    logger.info { "🔍 Labeling semantic roles in sentence: $sentence" }
    return callModel<SemanticRoles>(
        model,
        scope,
        prompt =
          ExpertSystem(
            system =
              "You are an expert in semantic role labeling that can identify the role of each word in a sentence",
            query =
              """|
                |Given the following sentence:
                |```sentence
                |${sentence}
                |```
            """
                .trimMargin(),
            instructions =
              listOf(
                "Label the role of each word in the `sentence`",
                "Your `RESPONSE` MUST be a list of `SemanticRole` objects, where each object has the `word` and its `role`"
              ) + instructions
          )
      )
      .also { logger.info { "🔍 Semantic role labeling result: $it" } }
  }
}
