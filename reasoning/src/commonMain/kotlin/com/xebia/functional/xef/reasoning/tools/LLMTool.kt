package com.xebia.functional.xef.reasoning.tools

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.jvm.JvmStatic

abstract class LLMTool(
  override val name: String,
  override val description: String,
  private val model: Chat,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) : Tool {
  private val logger = KotlinLogging.logger {}

  override suspend operator fun invoke(input: String): String {
    logger.info { "🔧 Running $name - $description" }

    return callModel(
        model,
        scope,
        prompt =
          ExpertSystem(
            system = "You are an expert in `$name` ($description)",
            query =
              """|
                |Given the following input:
                |```input
                |${input}
                |```
                |Produce an output that satisfies the tool `$name` ($description) operation.
            """
                .trimMargin(),
            instructions = instructions
          )
      )
      .also {
        logger.info { "🔧 Finished running $name - $description" }
        logger.info { "🔧 Output: $it" }
      }
  }

  companion object {
    @JvmStatic
    @JvmOverloads
    fun create(
      name: String,
      description: String,
      model: Chat,
      scope: CoreAIScope,
      instructions: List<String> = emptyList()
    ): LLMTool = object : LLMTool(name, description, model, scope, instructions) {}
  }
}
