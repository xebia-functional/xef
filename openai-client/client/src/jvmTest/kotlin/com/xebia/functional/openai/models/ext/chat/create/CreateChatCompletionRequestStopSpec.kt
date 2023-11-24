package com.xebia.functional.openai.models.ext.chat.create

import com.xebia.functional.simpleStringArb
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement

class CreateChatCompletionRequestStopSpec :
  StringSpec({
    "should serialize properly strings" {
      checkAll<String> { string ->
        val v: CreateChatCompletionRequestStop = CreateChatCompletionRequestStop.StringValue(string)
        Json.encodeToJsonElement<CreateChatCompletionRequestStop>(v) shouldBe JsonPrimitive(string)
      }
    }

    "should serialize properly arrays" {
      checkAll<List<String>> { list ->
        val v: CreateChatCompletionRequestStop = CreateChatCompletionRequestStop.ArrayValue(list)
        Json.encodeToJsonElement<CreateChatCompletionRequestStop>(v) shouldBe
          JsonArray(list.map { JsonPrimitive(it) })
      }
    }

    @Serializable data class MyValue(val value: CreateChatCompletionRequestStop)

    "should deserialize properly strings" {
      checkAll(simpleStringArb) { string ->
        val rawJson = """{ "value": "$string" }"""
        Json.decodeFromString<MyValue>(rawJson) shouldBe
          MyValue(CreateChatCompletionRequestStop.StringValue(string))
      }
    }

    "should deserialize properly arrays" {
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
  })
