package com.xebia.functional.xef.google

import com.google.cloud.aiplatform.v1.EndpointName
import com.google.cloud.aiplatform.v1.PredictResponse
import com.google.cloud.aiplatform.v1.PredictionServiceClient
import com.google.protobuf.Value
import com.xebia.functional.tokenizer.EncodingType
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.Completion
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.text.CompletionChoice
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult
import com.xebia.functional.xef.llm.models.usage.Usage
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.util.*

interface GoogleModel : AutoCloseable, Chat, Completion {

  val project: String
  val location: String
  val model: String

  override fun close() {
  }

  companion object {

    operator fun invoke(
      project: String,
      location: String,
      model: String
    ): GoogleModel = object : GoogleModel {

      private val json = Json { encodeDefaults = true }

      override val project: String = project
      override val location: String = location
      override val model: String = model

      val client = PredictionServiceClient.create()

      override suspend fun createCompletion(request: CompletionRequest): CompletionResult =
        with(request) {
          val responses: List<String> = generateCompletions(
            prompt,
            temperature ?: 0.0,
            topP ?: 0.0,
            maxTokens,
            frequencyPenalty,
            presencePenalty
          )
          return CompletionResult(
            UUID.randomUUID().toString(),
            model,
            System.currentTimeMillis(),
            model,
            responses.map { response ->
              CompletionChoice(response, 0, null, null)
            },
            Usage.ZERO
          )
        }

      override suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse =
        with(request) {
          val prompt: String = messages.buildPrompt()
          val responses: List<String> = generateCompletions(
            prompt,
            temperature,
            topP,
            maxTokens,
            frequencyPenalty,
            presencePenalty
          )
          return ChatCompletionResponse(
            UUID.randomUUID().toString(),
            model,
            System.currentTimeMillis().toInt(),
            model,
            Usage.ZERO,
            responses.map { response ->
              Choice(Message(Role.ASSISTANT, response, Role.ASSISTANT.name), null, 0)
            }
          )
        }

      override fun tokensFromMessages(messages: List<Message>): Int {
        return 0
      }

      override val name: String = model

      override fun close(): Unit = client.close()

      override val modelType: ModelType = ModelType.LocalModel(name, EncodingType.CL100K_BASE, 4096)

      private fun List<Message>.buildPrompt(): String {
        val messages: String = joinToString("") { message ->
          when (message.role) {
            Role.SYSTEM -> message.content
            Role.USER -> "\n### Human: ${message.content}"
            Role.ASSISTANT -> "\n### Response: ${message.content}"
          }
        }
        return "$messages\n### Response:"
      }

      private fun generateCompletions(
        prompt: String,
        temperature: Double,
        topP: Double,
        maxTokens: Int?,
        frequencyPenalty: Double,
        presencePenalty: Double
      ): List<String> {
        val endpointName = EndpointName.ofProjectLocationEndpointName(project, location, model)

        val parametersMap = mapOf(
          "prompt" to prompt,
          "temperature" to temperature,
          "topP" to topP,
          "maxOutputTokens" to maxTokens,
          "frequencyPenalty" to frequencyPenalty,
          "presencePenalty" to presencePenalty
        )

        val parameters: Value = Value.newBuilder().setStringValue(json.encodeToString(serializer(), parametersMap)).build()
        val response: PredictResponse = client.predict(endpointName, emptyList(), parameters)
        return response.predictionsList.map { it.stringValue }
      }
    }
  }
}
