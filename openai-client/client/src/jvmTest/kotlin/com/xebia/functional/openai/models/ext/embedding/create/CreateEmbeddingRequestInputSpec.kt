package com.xebia.functional.openai.models.ext.embedding.create

import com.xebia.functional.simpleStringArb
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement

class CreateEmbeddingRequestInputSpec {
  @Test
  fun shouldSerializeProperlyStrings() {
    runTest {
      checkAll<String> { string ->
        val v: CreateEmbeddingRequestInput = CreateEmbeddingRequestInput.StringValue(string)
        Json.encodeToJsonElement<CreateEmbeddingRequestInput>(v) shouldBe JsonPrimitive(string)
      }
    }
  }

  @Test
  fun shouldSerializeProperlyStringArrays() {
    runTest {
      checkAll<List<String>> { list ->
        val v: CreateEmbeddingRequestInput = CreateEmbeddingRequestInput.StringArrayValue(list)
        Json.encodeToJsonElement<CreateEmbeddingRequestInput>(v) shouldBe
          JsonArray(list.map { JsonPrimitive(it) })
      }
    }
  }

  @Test
  fun shouldSerializeProperlyIntArrays() {
    runTest {
      checkAll<List<Int>> { list ->
        val v: CreateEmbeddingRequestInput = CreateEmbeddingRequestInput.IntArrayValue(list)
        Json.encodeToJsonElement<CreateEmbeddingRequestInput>(v) shouldBe
          JsonArray(list.map { JsonPrimitive(it) })
      }
    }
  }

  @Test
  fun shouldSerializeProperlyIntArraysArrays() {
    runTest {
      checkAll<List<List<Int>>> { lists ->
        val v: CreateEmbeddingRequestInput = CreateEmbeddingRequestInput.IntArrayArrayValue(lists)
        Json.encodeToJsonElement<CreateEmbeddingRequestInput>(v) shouldBe
          JsonArray(lists.map { list -> JsonArray(list.map { JsonPrimitive(it) }) })
      }
    }
  }

  @Serializable data class MyValue(val value: CreateEmbeddingRequestInput)

  fun toJsonArray(list: List<Int>): String = list.joinToString(",\n", "[", "]") { "$it" }

  @Test
  fun shouldDeserializeProperlyStrings() {
    runTest {
      checkAll(simpleStringArb) { string ->
        val rawJson = """{ "value": "$string" }"""
        Json.decodeFromString<MyValue>(rawJson) shouldBe
          MyValue(CreateEmbeddingRequestInput.StringValue(string))
      }
    }
  }

  @Test
  fun shouldDeserializeProperlyStringArrays() {
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
          MyValue(CreateEmbeddingRequestInput.StringArrayValue(list))
      }
    }
  }

  @Test
  fun shouldDeserializeProperlyIntArrays() {
    runTest {
      checkAll(Arb.list(Arb.int(), range = 1..100)) { list ->
        val rawJson =
          """
          |{
          |  "value": ${toJsonArray(list)}
          |}
          |"""
            .trimMargin()
        Json.decodeFromString<MyValue>(rawJson) shouldBe
          MyValue(CreateEmbeddingRequestInput.IntArrayValue(list))
      }
    }
  }

  @Test
  fun shouldDeserializeProperlyIntArraysArrays() {
    runTest {
      checkAll(Arb.list(Arb.list(Arb.int()), range = 1..100)) { lists ->
        val rawJson =
          """
          |{
          |  "value": [
          |    ${lists.joinToString(",\n") { toJsonArray(it) }}
          |  ]
          |}
          |"""
            .trimMargin()
        Json.decodeFromString<MyValue>(rawJson) shouldBe
          MyValue(CreateEmbeddingRequestInput.IntArrayArrayValue(lists))
      }
    }
  }
}
