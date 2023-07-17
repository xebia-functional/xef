import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.tools.ToolMetadata
import io.github.oshai.kotlinlogging.KotlinLogging

class CodeBreakdown(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) : Tool<CodeBreakdownResult> {

  private val logger = KotlinLogging.logger {}

  override val functions:
    Map<ToolMetadata, suspend (input: String) -> Tool.Out<CodeBreakdownResult>> =
    mapOf(ToolMetadata(name = "breakDownCode", description = "Break down code") to ::breakDownCode)

  suspend fun breakDownCode(code: String): CodeBreakdownResult {
    logger.info { "🔍 Breaking down code into smaller functions" }
    return callModel<CodeBreakdownResult>(
        model,
        scope,
        ExpertSystem(
          system =
            "You are an expert in code breakdown that can break down complex long functions or classes into smaller functions that are more readable and have better semantics",
          query =
            """|
                    |Given the following code:
                    |```code
                    |${code}
                    |```
                """
              .trimMargin(),
          instructions =
            listOf(
              "Break down the `code` into smaller functions",
              "Your `RESPONSE` MUST contain `breakdownCode` (the code after breaking it down into smaller functions better named and more semantic)"
            ) + instructions
        )
      )
      .also { logger.info { "🔍 Code breakdown result: $it" } }
  }
}
