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
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.orNull
import io.kotest.property.checkAll
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*

@OptIn(ExperimentalSerializationApi::class)
class ChatCompletionRequestMessageSpec :
  StringSpec({
    val json = Json { explicitNulls = false }
    fun toJsonObject(call: ChatCompletionMessageToolCall): JsonObject =
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

    "should serialize properly assistant" {
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

    "should serialize properly system" {
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

    "should serialize properly tool" {
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

    "should serialize properly user" {
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

    "should deserialize properly assistant" {
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
          assistant.toolCalls
            ?.map { tool ->
              """
              | {
              |   "id": "${tool.id}",
              |   "type": "${tool.type.value}",
              |   "function": ${functionObject(tool.function)}
              | }
            """
                .trimMargin()
            }
            ?.joinToString(",", "[", "]")
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

    "should deserialize properly system" {
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

    "should deserialize properly tool" {
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

    "should deserialize properly user" {
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
  })
