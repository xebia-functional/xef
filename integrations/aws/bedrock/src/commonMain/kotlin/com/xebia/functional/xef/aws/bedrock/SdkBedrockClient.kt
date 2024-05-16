package com.xebia.functional.xef.aws.bedrock

import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.closeable
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.bedrockruntime.BedrockRuntimeClient
import aws.sdk.kotlin.services.bedrockruntime.model.InvokeModelRequest
import aws.sdk.kotlin.services.bedrockruntime.model.InvokeModelWithResponseStreamRequest
import aws.smithy.kotlin.runtime.client.LogMode
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.aws.bedrock.conf.Environment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

suspend fun ResourceScope.sdkClient(
  awsEnv: Environment.Aws,
  logMode: LogMode = LogMode.Default
): BedrockRuntimeClient = closeable {
  BedrockRuntimeClient {
    this.logMode = logMode
    region = awsEnv.regionName
    credentialsProvider =
      StaticCredentialsProvider(
        aws.smithy.kotlin.runtime.auth.awscredentials.Credentials.invoke(
          awsEnv.credentials.accessKeyId,
          awsEnv.credentials.secretAccessKey.value
        )
      )
  }
}

class SdkBedrockClient(private val client: BedrockRuntimeClient) : BedrockClient {
  override suspend fun runInference(
    requestBody: JsonElement,
    model: AwsFoundationModel
  ): ChatCompletionResponse {
    val invokeModelRequest =
      InvokeModelRequest.invoke {
        modelId = model.awsName
        accept = "application/json"
        contentType = "application/json"
        body = Json.encodeToString(requestBody).toByteArray()
      }

    val responseBody = String(client.invokeModel(invokeModelRequest).body)

    return Json.decodeFromString<ChatCompletionResponse>(responseBody)
  }

  /**
   * event: message_start data: {"type": "message_start", "message": {"id":
   * "msg_1nZdL29xx5MUA1yADyHTEsnR8uuvGzszyY", "type": "message", "role": "assistant", "content":
   * [], "model": "claude-3-opus-20240229", "stop_reason": null, "stop_sequence": null, "usage":
   * {"input_tokens": 25, "output_tokens": 1}}}
   *
   * event: content_block_start data: {"type": "content_block_start", "index": 0, "content_block":
   * {"type": "text", "text": ""}}
   *
   * event: ping data: {"type": "ping"}
   *
   * event: content_block_delta data: {"type": "content_block_delta", "index": 0, "delta": {"type":
   * "text_delta", "text": "Hello"}}
   *
   * event: content_block_delta data: {"type": "content_block_delta", "index": 0, "delta": {"type":
   * "text_delta", "text": "!"}}
   *
   * event: content_block_stop data: {"type": "content_block_stop", "index": 0}
   *
   * event: message_delta data: {"type": "message_delta", "delta": {"stop_reason": "end_turn",
   * "stop_sequence":null}, "usage": {"output_tokens": 15}}
   *
   * event: message_stop data: {"type": "message_stop"}
   *
   * This are what the ChatCompletionStreamEvent types and their data look like We model the data
   * part which already includes the type
   */
  @Serializable
  enum class EventType {
    message_start,
    content_block_start,
    ping,
    content_block_delta,
    content_block_stop,
    message_delta,
    message_stop
  }

  @Serializable
  data class ChatCompletionStreamEvent(
    val type: EventType,
    val message: ChatCompletionResponseEvent
  )

  @Serializable
  sealed class ChatCompletionResponseEvent {
    @Serializable
    data class MessageStart(
      val id: String,
      val type: String,
      val role: String,
      val content: List<ChatCompletionResponse.Content>,
      val model: String,
      @SerialName("stop_reason") val stopReason: String?,
      @SerialName("stop_sequence") val stopSequence: String?,
      val usage: ChatCompletionResponse.Usage
    ) : ChatCompletionResponseEvent()

    @Serializable
    data class ContentBlockStart(
      @SerialName("content_block") val contentBlock: ChatCompletionResponse.Content
    ) : ChatCompletionResponseEvent()

    @Serializable data class Ping(val type: String) : ChatCompletionResponseEvent()

    @Serializable
    data class ContentBlockDelta(val index: Int, val delta: Delta) : ChatCompletionResponseEvent()

    @Serializable data class Delta(val type: String? = null, val text: String? = null)

    @Serializable data class ContentBlockStop(val index: Int) : ChatCompletionResponseEvent()

    @Serializable
    data class MessageDelta(val delta: Delta, val usage: ChatCompletionResponse.Usage? = null) :
      ChatCompletionResponseEvent()

    @Serializable data class MessageStop(val type: String) : ChatCompletionResponseEvent()
  }

  @OptIn(ExperimentalSerializationApi::class)
  override fun runInferenceWithStream(
    requestBody: JsonElement,
    model: AwsFoundationModel
  ): Flow<ChatCompletionResponseEvent> {
    val json = Json {
      explicitNulls = false
      ignoreUnknownKeys = true
    }
    val streamRequest =
      InvokeModelWithResponseStreamRequest.invoke {
        modelId = model.awsName
        accept = "application/json"
        contentType = "application/json"
        body = Json.encodeToString(requestBody).toByteArray()
      }

    return channelFlow {
      client.invokeModelWithResponseStream(streamRequest) { response ->
        response.body?.collect { responseStream ->
          val chunk = responseStream.asChunkOrNull()?.bytes?.toString(Charsets.UTF_8)
          chunk?.let {
            val jsonEvent = json.parseToJsonElement(it).jsonObject
            val event = jsonEvent["type"]?.jsonPrimitive?.content
            if (event != null) {
              val responseEvent = serverSentEventToChatCompletionStreamEvent(event, chunk)
              send(responseEvent)
            }
          }
        }
      }
    }
  }

  fun serverSentEventToChatCompletionStreamEvent(
    event: String,
    data: String
  ): ChatCompletionResponseEvent {
    val eventType = EventType.valueOf(event)
    val json = Config.DEFAULT.json
    val jsonData = json.parseToJsonElement(data).jsonObject
    return when (eventType) {
      EventType.message_start ->
        json.decodeFromJsonElement(
          ChatCompletionResponseEvent.MessageStart.serializer(),
          jsonData["message"]!!
        )
      EventType.content_block_start ->
        json.decodeFromJsonElement(
          ChatCompletionResponseEvent.ContentBlockStart.serializer(),
          jsonData
        )
      EventType.ping -> ChatCompletionResponseEvent.Ping(jsonData["type"]!!.jsonPrimitive.content)
      EventType.content_block_delta ->
        json.decodeFromJsonElement(
          ChatCompletionResponseEvent.ContentBlockDelta.serializer(),
          jsonData
        )
      EventType.content_block_stop ->
        ChatCompletionResponseEvent.ContentBlockStop(
          jsonData["index"]!!.jsonPrimitive.content.toInt()
        )
      EventType.message_delta ->
        json.decodeFromJsonElement(ChatCompletionResponseEvent.MessageDelta.serializer(), jsonData)
      EventType.message_stop ->
        ChatCompletionResponseEvent.MessageStop(jsonData["type"]!!.jsonPrimitive.content)
    }
  }
}
