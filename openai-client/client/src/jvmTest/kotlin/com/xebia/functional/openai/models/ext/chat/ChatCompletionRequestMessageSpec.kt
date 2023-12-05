package com.xebia.functional.com.xebia.functional.openai.models.ext.chat

import com.xebia.functional.chatCompletionRequestAssistantMessage
import com.xebia.functional.chatCompletionRequestToolMessage
import com.xebia.functional.openai.models.ChatCompletionMessageToolCall
import com.xebia.functional.openai.models.ChatCompletionMessageToolCallFunction
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestMessage
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestSystemMessage
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestToolMessage
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestUserMessage
import com.xebia.functional.simpleStringArb
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.orNull
import io.kotest.property.checkAll
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*

@OptIn(ExperimentalSerializationApi::class)
class ChatCompletionRequestMessageSpec {
  private val json = Json { explicitNulls = false }

  private fun toJsonObject(call: ChatCompletionMessageToolCall): JsonObject =
    JsonObject(
      mapOf(
        "id" to JsonPrimitive(call.id),
        "type" to JsonPrimitive(call.type.value),
        "function" to
          JsonObject(
            mapOf(
              "name" to JsonPrimitive(call.function.name),
              "arguments" to JsonPrimitive(call.function.arguments)
            )
          )
      )
    )

  @Test
  fun shouldSerializeProperlyAssistant() {
    runTest {
      checkAll(chatCompletionRequestAssistantMessage) { assistant ->
        val v: ChatCompletionRequestMessage = assistant
        json.encodeToJsonElement<ChatCompletionRequestMessage>(v) shouldBe
          JsonObject(
            listOfNotNull(
                assistant.content?.let { "content" to JsonPrimitive(it) },
                "role" to JsonPrimitive(assistant.role.value),
                assistant.toolCalls?.let { calls ->
                  "tool_calls" to JsonArray(calls.map { toJsonObject(it) })
                }
              )
              .toMap()
          )
      }
    }
  }

  @Test
  fun shouldSerializeProperlySystem() {
    runTest {
      checkAll(simpleStringArb.orNull(0.2)) { content ->
        val v: ChatCompletionRequestMessage = ChatCompletionRequestSystemMessage(content)
        json.encodeToJsonElement<ChatCompletionRequestMessage>(v) shouldBe
          JsonObject(
            listOfNotNull(
                content?.let { "content" to JsonPrimitive(it) },
                "role" to JsonPrimitive(ChatCompletionRequestSystemMessage.Role.system.value)
              )
              .toMap()
          )
      }
    }
  }

  @Test
  fun shouldSerializeProperlyTool() {
    runTest {
      checkAll(chatCompletionRequestToolMessage) { tool ->
        val v: ChatCompletionRequestMessage = tool
        json.encodeToJsonElement<ChatCompletionRequestMessage>(v) shouldBe
          JsonObject(
            listOfNotNull(
                tool.content?.let { "content" to JsonPrimitive(it) },
                "tool_call_id" to JsonPrimitive(tool.toolCallId),
                "role" to JsonPrimitive(ChatCompletionRequestToolMessage.Role.tool.value)
              )
              .toMap()
          )
      }
    }
  }

  @Test
  fun shouldSerializeProperlyUser() {
    // content serialization is covered at ChatCompletionRequestUserMessageContentSpec
    val v: ChatCompletionRequestMessage =
      ChatCompletionRequestUserMessage(listOf(), ChatCompletionRequestUserMessage.Role.user)
    json.encodeToJsonElement<ChatCompletionRequestMessage>(v) shouldBe
      JsonObject(
        mapOf(
            "content" to JsonArray(listOf()),
            "role" to JsonPrimitive(ChatCompletionRequestUserMessage.Role.user.value)
          )
          .toMap()
      )
  }

  @Test
  fun shouldDeserializeProperlyAssistant() {
    runTest {
      checkAll(chatCompletionRequestAssistantMessage) { assistant ->
        val contentOrEmpty = assistant.content?.let { """ "content" : "$it", """ } ?: ""
        fun functionObject(function: ChatCompletionMessageToolCallFunction): String =
          """
            |{
            |  "name": "${function.name}",
            |  "arguments": "${function.arguments}"
            |}
          """
            .trimMargin()
        val toolCallsObject =
          assistant.toolCalls?.joinToString(",", "[", "]") { tool ->
            """
              | {
              |   "id": "${tool.id}",
              |   "type": "${tool.type.value}",
              |   "function": ${functionObject(tool.function)}
              | }
            """
              .trimMargin()
          }
            ?: "[]"
        val rawJson =
          """
        |{
        |  $contentOrEmpty
        |  "role": "${assistant.role.value}",
        |  "tool_calls": $toolCallsObject
        |}"""
            .trimMargin()
        json.decodeFromString<ChatCompletionRequestMessage>(rawJson) shouldBe assistant
      }
    }
  }

  @Test
  fun shouldDeserializeProperlySystem() {
    runTest {
      checkAll(simpleStringArb.orNull(0.2)) { content ->
        val contentOrEmpty = content?.let { """ "content" : "$it", """ } ?: ""
        val rawJson =
          """
        |{
        |  $contentOrEmpty
        |  "role": "${ChatCompletionRequestSystemMessage.Role.system.value}"
        |}"""
            .trimMargin()
        json.decodeFromString<ChatCompletionRequestMessage>(rawJson) shouldBe
          ChatCompletionRequestSystemMessage(content)
      }
    }
  }

  @Test
  fun shouldDeserializeProperlyTool() {
    runTest {
      checkAll(chatCompletionRequestToolMessage) { tool ->
        val contentOrEmpty = tool.content?.let { """ "content" : "$it", """ } ?: ""
        val rawJson =
          """
        |{
        |  $contentOrEmpty
        |  "tool_call_id": "${tool.toolCallId}",
        |  "role": ${tool.role.value}
        |}"""
            .trimMargin()
        json.decodeFromString<ChatCompletionRequestMessage>(rawJson) shouldBe tool
      }
    }
  }

  @Test
  fun shouldDeserializeProperlyUser() {
    // content serialization is covered at ChatCompletionRequestUserMessageContentSpec
    val rawJson =
      """
      |{
      |  "content": [],
      |  "role": ${ChatCompletionRequestUserMessage.Role.user.value}
      |}"""
        .trimMargin()
    json.decodeFromString<ChatCompletionRequestMessage>(rawJson) shouldBe
      ChatCompletionRequestUserMessage(listOf())
  }
}
