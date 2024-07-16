package com.xebia.functional.xef.functions

import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.conversation.Schema
import com.xebia.functional.xef.llm.chatFunction
import com.xebia.functional.xef.llm.defaultFunctionDescription
import com.xebia.functional.xef.llm.functionName
import com.xebia.functional.xef.llm.models.functions.buildJsonSchema
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

class FunctionSchemaTests :
  StringSpec({
    "Request has default description" {
      val descriptor = Request.serializer().descriptor
      val function = chatFunction(descriptor)
      val fnName = functionName(descriptor)
      function.description shouldBe defaultFunctionDescription(fnName)
    }

    "Description can be set on request" {
      val descriptor = RequestWithDescription.serializer().descriptor
      val function = chatFunction(descriptor)
      function.description shouldBe "Request With Description"
    }

    "Schema can be generated on request" {
      val descriptor = Request.serializer().descriptor
      val function = chatFunction(descriptor)
      function.parameters shouldBe buildJsonSchema(descriptor)
    }

    "Schema can be set on request" {
      val descriptor = RequestWithSchema.serializer().descriptor
      val function = chatFunction(descriptor)
      function.parameters shouldBe JsonObject(emptyMap())
    }
  }) {

  @Serializable data class Request(val input: String)

  @Serializable
  @Description("Request With Description")
  data class RequestWithDescription(val input: String)

  @Serializable
  @Description("Request with schema")
  @Schema("""
    {
    }
  """)
  data class RequestWithSchema(val input: String)
}
