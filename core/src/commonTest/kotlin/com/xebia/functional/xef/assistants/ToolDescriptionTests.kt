package com.xebia.functional.xef.assistants

import com.xebia.functional.xef.llm.assistants.Tool
import com.xebia.functional.xef.llm.defaultFunctionDescription
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable

class ToolDescriptionTests :
  StringSpec({
    "Tool has tool name and default description" {
      val tool = TestTool()
      val toolConfig = Tool.toolOf(tool)
      val function = toolConfig.functionObject
      val fnName = tool::class.simpleName ?: error("unnamed class")
      function.name shouldBe fnName
      function.description shouldBe defaultFunctionDescription(fnName)
    }
  }) {

  @Serializable data class Request(val input: String)

  @Serializable data class Response(val output: String = "Test response")

  class TestTool : Tool<Request, Response> {
    override suspend fun invoke(input: Request): Response = Response()
  }
}
