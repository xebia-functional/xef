package com.xebia.functional.openai.models.ext.chat

import com.xebia.functional.contentImageUrlArb
import com.xebia.functional.simpleStringArb
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*

@OptIn(ExperimentalSerializationApi::class)
class ChatCompletionRequestUserMessageContentSpec {
  val json = Json { explicitNulls = false }
  val imageType = ChatCompletionRequestUserMessageContentImage.Type.imageUrl.value
  val textType = ChatCompletionRequestUserMessageContentText.Type.text.value

  @Test
  fun shouldSerializeProperlyImage() {
    runTest {
      checkAll(contentImageUrlArb) { imageUrl ->
        val v: ChatCompletionRequestUserMessageContent =
          ChatCompletionRequestUserMessageContentImage(imageUrl)
        json.encodeToJsonElement<ChatCompletionRequestUserMessageContent>(v) shouldBe
          JsonObject(
            mapOf(
              "type" to JsonPrimitive(imageType),
              "image_url" to
                JsonObject(
                  listOfNotNull(
                      imageUrl.url?.let { "url" to JsonPrimitive(it) },
                      imageUrl.detail?.let { "detail" to JsonPrimitive(it.value) }
                    )
                    .toMap()
                )
            )
          )
      }
    }
  }

  @Test
  fun shouldSerializeProperlyText() {
    runTest {
      checkAll(simpleStringArb) { text ->
        val v: ChatCompletionRequestUserMessageContent =
          ChatCompletionRequestUserMessageContentText(text)
        json.encodeToJsonElement<ChatCompletionRequestUserMessageContent>(v) shouldBe
          JsonObject(mapOf("type" to JsonPrimitive(textType), "text" to JsonPrimitive(text)))
      }
    }
  }

  @Test
  fun shouldDeserializeProperlyImage() {
    runTest {
      checkAll(contentImageUrlArb) { imageUrl ->
        val imageUrlObject =
          listOfNotNull(
              imageUrl.url?.let { "url" to it },
              imageUrl.detail?.let { "detail" to it.value }
            )
            .joinToString(",", "{", "}") { pair -> """ "${pair.first}": "${pair.second}"  """ }
        val rawJson =
          """
        |{ 
        |  "type": "$imageType", 
        |  "image_url": $imageUrlObject
        |}"""
            .trimMargin()
        json.decodeFromString<ChatCompletionRequestUserMessageContent>(rawJson) shouldBe
          ChatCompletionRequestUserMessageContentImage(imageUrl)
      }
    }
  }

  @Test
  fun shouldDeserializeProperlyText() {
    runTest {
      checkAll(simpleStringArb) { text ->
        val rawJson =
          """
        |{ 
        |  "type": "$textType", 
        |  "text": "$text"
        |}"""
            .trimMargin()
        json.decodeFromString<ChatCompletionRequestUserMessageContent>(rawJson) shouldBe
          ChatCompletionRequestUserMessageContentText(text)
      }
    }
  }
}
