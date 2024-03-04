package com.xebia.functional.openai.models.ext.assistant

import com.xebia.functional.functionObjectArb
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

class AssistantToolsSpec {
  @Test
  fun shouldSerializeProperlyCode() {
    val v: AssistantTools = AssistantToolsCode()
    Json.encodeToJsonElement<AssistantTools>(v) shouldBe
      JsonObject(mapOf("type" to JsonPrimitive(AssistantToolsCode.Type.code_interpreter.value)))
  }

  @Test
  fun shouldSerializeProperlyFunction() {
    runTest {
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
                      "parameters" to (fo.parameters ?: JsonObject(emptyMap())),
                      fo.description?.let { "description" to JsonPrimitive(it) }
                    )
                    .toMap()
                )
            )
          )
      }
    }
  }

  @Test
  fun shouldSerializeProperlyRetrieval() {
    val v: AssistantTools = AssistantToolsRetrieval()
    Json.encodeToJsonElement<AssistantTools>(v) shouldBe
      JsonObject(mapOf("type" to JsonPrimitive(AssistantToolsRetrieval.Type.retrieval.value)))
  }

  @Test
  fun shouldDeserializeProperlyCode() {
    val rawJson = """{"type":"${AssistantToolsCode.Type.code_interpreter.value}"}"""
    Json.decodeFromString<AssistantTools>(rawJson) shouldBe AssistantToolsCode()
  }

  @Test
  fun shouldDeserializeProperlyFunction() {
    runTest {
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
  }

  @Test
  fun shouldDeserializeProperlyRetrieval() {
    val rawJson = """{"type":"${AssistantToolsRetrieval.Type.retrieval.value}"}"""
    Json.decodeFromString<AssistantTools>(rawJson) shouldBe AssistantToolsRetrieval()
  }
}
