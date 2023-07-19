package com.xebia.functional.xef.reasoning.text.entities

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class EntityRecognition(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}

  suspend fun recognizeEntities(text: String, entities: List<String>): EntityResults {
    logger.info { "🔍 Recognizing entities in text: ${text.length}" }
    return callModel<EntityResults>(
        model,
        scope,
        prompt =
          ExpertSystem(
            system =
              "You are an expert in entity recognition that can identify mentions of specific entities in a piece of text",
            query =
              """|
                |Given the following text:
                |```text
                |${text}
                |```
                |And the following list of entities:
                |```entities
                |${entities.joinToString(", ")}
                |```
            """
                .trimMargin(),
            instructions =
              listOf(
                "Scan the `text` and identify mentions of the entities",
                "Your `RESPONSE` MUST be a list of `EntityResult` objects, where each object has the `entity` and a shorted list of the `mentions` with up to 10 words in the text where the entity is mentioned"
              ) + instructions
          )
      )
      
  }
}
