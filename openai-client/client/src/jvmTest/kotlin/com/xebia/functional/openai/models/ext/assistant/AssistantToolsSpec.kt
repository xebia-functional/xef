package com.xebia.functional.openai.models.ext.assistant

import com.xebia.functional.functionObjectArb
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

class AssistantToolsSpec :
  StringSpec({
    "should serialize properly code" {
      val v: AssistantTools = AssistantToolsCode()
      Json.encodeToJsonElement<AssistantTools>(v) shouldBe
        JsonObject(mapOf("type" to JsonPrimitive(AssistantToolsCode.Type.code_interpreter.value)))
    }

    "should serialize properly function" {
      checkAll(functionObjectArb) { fo ->
        val v: AssistantTools = AssistantToolsFunction(fo)
        val encodedValue = Json.encodeToJsonElement<AssistantTools>(v)
        encodedValue shouldBe
          JsonObject(
            mapOf(
              "type" to JsonPrimitive(AssistantToolsFunction.Type.function.value),
              "function" to
                JsonObject(
                  listOfNotNull(
                      "name" to JsonPrimitive(fo.name),
                      "parameters" to fo.parameters,
                      fo.description?.let { "description" to JsonPrimitive(it) }
                    )
                    .toMap()
                )
            )
          )
      }
    }

    "should serialize properly retrieval" {
      val v: AssistantTools = AssistantToolsRetrieval()
      Json.encodeToJsonElement<AssistantTools>(v) shouldBe
        JsonObject(mapOf("type" to JsonPrimitive(AssistantToolsRetrieval.Type.retrieval.value)))
    }

    "should deserialize properly code" {
      val rawJson = """{"type":"${AssistantToolsCode.Type.code_interpreter.value}"}"""
      Json.decodeFromString<AssistantTools>(rawJson) shouldBe AssistantToolsCode()
    }

    "should deserialize properly function" {
      checkAll(functionObjectArb) { fo ->
        val rawJson =
          """
          |{
          |  "type": "${AssistantToolsFunction.Type.function.value}",
          |  "function": ${Json.encodeToString(fo)}
          |}
        """
            .trimMargin()
        Json.decodeFromString<AssistantTools>(rawJson) shouldBe AssistantToolsFunction(fo)
      }
    }

    "should deserialize properly retrieval" {
      val rawJson = """{"type":"${AssistantToolsRetrieval.Type.retrieval.value}"}"""
      Json.decodeFromString<AssistantTools>(rawJson) shouldBe AssistantToolsRetrieval()
    }
  })
