package com.xebia.functional.xef

import ai.xef.openai.StandardModel
import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.models.CreateChatCompletionRequest
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestUserMessage
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestUserMessageContentText
import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.fromEnvironment
import com.xebia.functional.xef.llm.models.modelType
import com.xebia.functional.xef.llm.prompt
import com.xebia.functional.xef.prompt.Prompt
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.serializer
import kotlin.jvm.JvmInline

interface AI<A> {
  val model: CreateChatCompletionRequestModel
  val api: ChatApi
  val serializer: () -> KSerializer<A>
  val conversation: Conversation
  val enumSerializer: ((case: String) -> A)?

  @Serializable
  data class Value<A>(val value: A)

  private suspend fun <B> runWithSerializer(prompt: String, serializer: KSerializer<B>): B =
    api.prompt(Prompt(StandardModel(model), prompt), conversation, serializer)

  @OptIn(ExperimentalSerializationApi::class)
  suspend operator fun invoke(prompt: String): A {
    val serializer = serializer()
    return when (serializer.descriptor.kind) {
      PrimitiveKind.BOOLEAN -> runWithSerializer(prompt, Value.serializer(Boolean.serializer())).value as A
      PrimitiveKind.BYTE -> runWithSerializer(prompt, Value.serializer(Byte.serializer())).value as A
      PrimitiveKind.CHAR -> runWithSerializer(prompt, Value.serializer(Char.serializer())).value as A
      PrimitiveKind.DOUBLE -> runWithSerializer(prompt, Value.serializer(Double.serializer())).value as A
      PrimitiveKind.FLOAT -> runWithSerializer(prompt, Value.serializer(Float.serializer())).value as A
      PrimitiveKind.INT -> runWithSerializer(prompt, Value.serializer(Int.serializer())).value as A
      PrimitiveKind.LONG -> runWithSerializer(prompt, Value.serializer(Long.serializer())).value as A
      PrimitiveKind.SHORT -> runWithSerializer(prompt, Value.serializer(Short.serializer())).value as A
      PrimitiveKind.STRING -> runWithSerializer(prompt, Value.serializer(String.serializer())).value as A
      SerialKind.ENUM -> {
        val encoding = StandardModel(model).modelType(forFunctions = false).encoding
        val cases = serializer.descriptor.elementDescriptors.map { it.serialName.substringAfterLast(".") }
        val logitBias = cases.flatMap {
          val result = encoding.encode(it)
          if (result.size > 1) {
            error("Cannot encode enum case $it into one token")
          }
          result
        }.associate { "$it" to 100 }
        val result = api.createChatCompletion(CreateChatCompletionRequest(
          messages = listOf(ChatCompletionRequestUserMessage(
            content = listOf(ChatCompletionRequestUserMessageContentText(prompt)),
          )),
          model = StandardModel(model),
          logitBias = logitBias,
          maxTokens = 1,
          temperature = 0.0
        ))
        val choice = result.body().choices[0].message.content
        val enumSerializer = enumSerializer
        if (choice != null && enumSerializer != null) {
          enumSerializer(choice)
        } else {
          error("Cannot decode enum case from $choice")
        }
      }
      else -> runWithSerializer(prompt, serializer)
    }
  }

  companion object {
    @PublishedApi
    internal operator fun <A: Any> invoke(
      model: CreateChatCompletionRequestModel,
      api: ChatApi,
      conversation: Conversation,
      enumSerializer: ((case: String) -> A)?,
      serializer: () -> KSerializer<A>,
    ): AI<A> = object : AI<A> {
      override val model: CreateChatCompletionRequestModel = model
      override val api: ChatApi = api
      override val serializer: () -> KSerializer<A> = serializer
      override val conversation: Conversation = conversation
      override val enumSerializer: ((case: String) -> A)? = enumSerializer
    }

    @AiDsl
    inline fun <reified A: Enum<A>> enum(
      model: CreateChatCompletionRequestModel = CreateChatCompletionRequestModel.gpt_4_1106_preview,
      api: ChatApi = fromEnvironment(::ChatApi)
    ): AI<A> = invoke(model, api, Conversation(), { name ->
      enumValueOf<A>(name)
    }) { serializer() }

    inline operator fun <reified A: Any> invoke(
      model: CreateChatCompletionRequestModel = CreateChatCompletionRequestModel.gpt_4_1106_preview,
      api: ChatApi = fromEnvironment(::ChatApi),
      conversation: Conversation = Conversation()
    ): AI<A> = invoke(model, api, conversation, null) { serializer() }

    suspend inline operator fun <reified A: Any> invoke(
      prompt: String,
      model: CreateChatCompletionRequestModel = CreateChatCompletionRequestModel.gpt_4_1106_preview,
      api: ChatApi = fromEnvironment(::ChatApi),
      conversation: Conversation = Conversation()
    ): A = invoke(model, api, conversation, null) { serializer<A>() }.invoke(prompt)

  }
}
