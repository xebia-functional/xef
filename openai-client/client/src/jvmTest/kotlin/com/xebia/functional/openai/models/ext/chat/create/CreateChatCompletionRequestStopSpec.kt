package com.xebia.functional.openai.models.ext.chat.create

import com.xebia.functional.simpleStringArb
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement

class CreateChatCompletionRequestStopSpec {
  @Test
  fun shouldSerializeProperlyStrings() {
    runTest {
      checkAll<String> { string ->
        val v: CreateChatCompletionRequestStop = CreateChatCompletionRequestStop.StringValue(string)
        Json.encodeToJsonElement<CreateChatCompletionRequestStop>(v) shouldBe JsonPrimitive(string)
      }
    }
  }

  @Test
  fun shouldSerializeProperlyArrays() {
    runTest {
      checkAll<List<String>> { list ->
        val v: CreateChatCompletionRequestStop = CreateChatCompletionRequestStop.ArrayValue(list)
        Json.encodeToJsonElement<CreateChatCompletionRequestStop>(v) shouldBe
          JsonArray(list.map { JsonPrimitive(it) })
      }
    }
  }

  @Serializable data class MyValue(val value: CreateChatCompletionRequestStop)

  @Test
  fun shouldDeserializeProperlyStrings() {
    runTest {
      checkAll(simpleStringArb) { string ->
        val rawJson = """{ "value": "$string" }"""
        Json.decodeFromString<MyValue>(rawJson) shouldBe
          MyValue(CreateChatCompletionRequestStop.StringValue(string))
      }
    }
  }

  @Test
  fun shouldDeserializeProperlyArrays() {
    runTest {
      checkAll(Arb.list(simpleStringArb)) { list ->
        val rawJson =
          """
          |{ 
          |  "value": [
          |    ${list.joinToString(",\n") { "\"$it\"" }}
          |  ]  
          |}
          |"""
            .trimMargin()
        Json.decodeFromString<MyValue>(rawJson) shouldBe
          MyValue(CreateChatCompletionRequestStop.ArrayValue(list))
      }
    }
  }
}
