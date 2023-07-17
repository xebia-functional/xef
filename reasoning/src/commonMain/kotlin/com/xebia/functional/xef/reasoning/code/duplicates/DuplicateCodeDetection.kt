import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging

class DuplicateCodeDetection(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) {

  private val logger = KotlinLogging.logger {}

  suspend fun findDuplicateCode(
    sourceCode: String,
    similarityThreshold: Double
  ): DuplicateCodeDetectionResult {
    logger.info { "🔍 Finding duplicate code within the source" }
    return callModel<DuplicateCodeDetectionResult>(
        model,
        scope,
        prompt =
          ExpertSystem(
            system =
              "You are an expert in duplicate code detection that can find duplicate code within a single source input",
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
                "Find duplicate code within the `sourceCode`",
                "Your `RESPONSE` MUST contain `duplicateSnippets` (a list of duplicate code snippets), `similarityThreshold` (a double) indicating the minimum similarity threshold for considering code snippets as duplicates, and `suggestedRefactor` (a string) suggesting ONLY the code refactor after eliminating all duplications",
                "Consider simplifying the code where applicable to avoid the repetition of the same expressions, statements, or blocks of code",
              ) + instructions
          ),
      )
      .also { logger.info { "🔍 Duplicate code detection result: $it" } }
  }
}
