package com.xebia.functional.openai.models.ext.assistant

import com.xebia.functional.openai.models.RunStepDetailsMessageCreationObjectMessageCreation
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.uuid
import io.kotest.property.checkAll
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*

class RunStepObjectStepDetailsSpec {
  @Test
  fun shouldSerializeProperlyToolCalls() {
    val v: RunStepObjectStepDetails = RunStepDetailsToolCallsObject(listOf())
    Json.encodeToJsonElement<RunStepObjectStepDetails>(v) shouldBe
      JsonObject(
        mapOf(
          "type" to JsonPrimitive(RunStepDetailsToolCallsObject.Type.tool_calls.value),
          "tool_calls" to JsonArray(listOf())
        )
      )
  }

  @Test
  fun shouldSerializeProperlyMessageCreation() {
    runTest {
      checkAll(Arb.uuid().map { it.toString() }) { id ->
        val v: RunStepObjectStepDetails =
          RunStepDetailsMessageCreationObject(
            RunStepDetailsMessageCreationObjectMessageCreation(id)
          )
        Json.encodeToJsonElement<RunStepObjectStepDetails>(v) shouldBe
          JsonObject(
            mapOf(
              "type" to
                JsonPrimitive(RunStepDetailsMessageCreationObject.Type.message_creation.value),
              "message_creation" to JsonObject(mapOf("message_id" to JsonPrimitive(id)))
            )
          )
      }
    }
  }

  @Test
  fun shouldDeserializeProperlyToolCalls() {
    val rawJson =
      """
        |{ 
        |  "type": "${RunStepDetailsToolCallsObject.Type.tool_calls.value}", 
        |  "tool_calls": []
        |}"""
        .trimMargin()
    Json.decodeFromString<RunStepObjectStepDetails>(rawJson) shouldBe
      RunStepDetailsToolCallsObject(listOf())
  }

  @Test
  fun shouldDeserializeProperlyMessageCreation() {
    runTest {
      checkAll(Arb.uuid().map { it.toString() }) { id ->
        val rawJson =
          """
        |{ 
        |  "type": "${RunStepDetailsMessageCreationObject.Type.message_creation.value}", 
        |  "message_creation": { "message_id": "$id" }
        |}"""
            .trimMargin()
        Json.decodeFromString<RunStepObjectStepDetails>(rawJson) shouldBe
          RunStepDetailsMessageCreationObject(
            RunStepDetailsMessageCreationObjectMessageCreation(id)
          )
      }
    }
  }
}
