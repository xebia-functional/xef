package com.xebia.functional.xef.reasoning.code.tests

import com.xebia.functional.xef.reasoning.internals.callModel
import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import io.github.oshai.kotlinlogging.KotlinLogging

class TestGeneration(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope
) {

  private val logger = KotlinLogging.logger {}

  suspend fun generateTestCases(code: String): TestGenerationResult {
    logger.info { "🔍 Generating test cases for code: ${code.length}" }
    return callModel<TestGenerationResult>(
      model,
      scope,
      prompt = ExpertSystem(
        system = "You are an expert in test case generation that can generate different types of test cases for a given code",
        query = """|
                |Given the following code:
                |```code
                |${code}
                |```
                |
                |Please generate test cases of the following types:
                |- UNIT: Tests individual units or components in isolation.
                |- INTEGRATION: Tests the interaction between multiple components or modules.
                |- LAWS: Tests the compliance with laws or properties. Test expressed as laws are returned
                |  as functions that take the input as forall constrain quantifiers.
                |  Example:
                |  ```code
                |  val commutativeLaw: (Int, Int) -> Boolean = { a, b -> a + b == b + a }
                |  val associativeLaw: (Int, Int, Int) -> Boolean = { a, b, c -> (a + b) + c == a + (b + c) }
                |  val distributiveLaw: (Int, Int, Int) -> Boolean = { a, b, c -> a * (b + c) == a * b + a * c }
                |  val laws = listOf(commutativeLaw, associativeLaw, distributiveLaw)
                |  ```
                |- PERFORMANCE: Tests the performance and scalability of the code.
                |
                |Provide a set of test cases based on the code.
            """.trimMargin(),
        instructions = listOf(
          "Generate test cases based on the `code`",
          "Ensure to include test cases for each of the following types: UNIT, INTEGRATION, LAWS, PERFORMANCE",
          "Provide a description for each test case",
          "Your `RESPONSE` MUST be a list of `TestCase` objects, where each object has the `type` and `description`"
        )
      ),
    ).also {
      logger.info { "🔍 Test case generation result: $it" }
    }
  }
}
