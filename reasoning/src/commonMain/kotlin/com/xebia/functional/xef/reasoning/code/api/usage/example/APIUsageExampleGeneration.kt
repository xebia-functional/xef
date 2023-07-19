package com.xebia.functional.xef.reasoning.code.api.usage.example

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.tools.ToolMetadata
import com.xebia.functional.xef.reasoning.tools.ToolOutput
import io.github.oshai.kotlinlogging.KotlinLogging

class APIUsageExampleGeneration(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) : Tool<APIUsageExampleGenerationResult> {

  private val logger = KotlinLogging.logger {}

  override val functions:
    Map<ToolMetadata, suspend (input: String) -> Tool.Out<APIUsageExampleGenerationResult>> =
    mapOf(
      ToolMetadata(
        name = "generateUsageExamples",
        description = "Generate usage examples for a list of APIs"
      ) to ::generateUsageExamples
    )

  override suspend fun handle(input: ToolOutput<Any?>): Tool.Out<APIUsageExampleGenerationResult> {
    return generateUsageExamples(input.toOutputString())
  }

  suspend fun generateUsageExamples(api: String): APIUsageExampleGenerationResult {
    return generateUsageExamples(listOf(api))
  }

  suspend fun generateUsageExamples(apis: List<String>): APIUsageExampleGenerationResult {
    logger.info { "🔍 Generating API usage examples" }
    return callModel<APIUsageExampleGenerationResult>(
        model,
        scope,
        ExpertSystem(
          system =
            "You are an expert in API usage example generation that can generate usage examples for a list of APIs",
          query =
            """|
                |Given the following APIs:
                |```apis
                |${apis.joinToString(", ")}
                |```
                |
                |Please provide usage examples for each API.
            """
              .trimMargin(),
          instructions =
            listOf(
              "Generate usage examples for each of the given `apis`",
              "For each API, provide a `description` of how it is used and a `codeSnippet` demonstrating the usage",
              "Your `RESPONSE` MUST be a list of `APIUsageExample` objects, where each object has the `apiName`, `description`, and `codeSnippet`"
            ) + instructions
        )
      )
      
  }
}
