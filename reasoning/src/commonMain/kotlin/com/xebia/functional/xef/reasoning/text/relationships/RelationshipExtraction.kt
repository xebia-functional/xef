package com.xebia.functional.xef.reasoning.text.relationships

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class RelationshipExtraction(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}

  suspend fun extractRelationships(text: String): RelationshipResult {
    logger.info { "🔍 Extracting relationships from text: ${text.length}" }
    return callModel<RelationshipResult>(
        model,
        scope,
        prompt =
          ExpertSystem(
            system =
              "You are an expert in relationship extraction that can identify and extract the relationships between entities in a piece of text",
            query =
              """|
                |Given the following text:
                |```text
                |${text}
                |```
            """
                .trimMargin(),
            instructions =
              listOf(
                "Extract the relationships between entities in the `text`",
                "Your `RESPONSE` MUST be a `RelationshipResult` object, where each element in the list is a string describing a relationship between two or more entities"
              ) + instructions
          )
      )
      
  }
}
