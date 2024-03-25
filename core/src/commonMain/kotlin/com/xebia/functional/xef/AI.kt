package com.xebia.functional.xef

import ai.xef.openai.CustomModel
import ai.xef.openai.OpenAIModel
import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.apis.ImagesApi
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.fromEnvironment
import com.xebia.functional.xef.prompt.Prompt
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.serializer

sealed interface AI {

  companion object {

    fun <A : Any> chat(
      target: KType,
      model: OpenAIModel<CreateChatCompletionRequestModel>,
      api: ChatApi,
      conversation: Conversation,
      enumSerializer: ((case: String) -> A)?,
      caseSerializers: List<KSerializer<A>>,
      serializer: () -> KSerializer<A>,
    ): Chat<A> =
      Chat(
        target = target,
        model = model,
        api = api,
        serializer = serializer,
        conversation = conversation,
        enumSerializer = enumSerializer,
        caseSerializers = caseSerializers
      )

    fun images(
      api: ImagesApi = fromEnvironment(::ImagesApi),
      chatApi: ChatApi = fromEnvironment(::ChatApi)
    ): Images = Images(api, chatApi)

    @PublishedApi
    internal suspend inline fun <reified A : Any> invokeEnum(
      prompt: Prompt<CreateChatCompletionRequestModel>,
      target: KType = typeOf<A>(),
      api: ChatApi = fromEnvironment(::ChatApi),
      conversation: Conversation = Conversation()
    ): A =
      chat(
          target = target,
          model = prompt.model,
          api = api,
          conversation = conversation,
          enumSerializer = { @Suppress("UPPER_BOUND_VIOLATED") enumValueOf<A>(it) },
          caseSerializers = emptyList()
        ) {
          serializer<A>()
        }
        .invoke(prompt)

    @AiDsl
    suspend inline operator fun <reified A : Any> invoke(
      prompt: String,
      target: KType = typeOf<A>(),
      model: CreateChatCompletionRequestModel = CreateChatCompletionRequestModel.gpt_4_1106_preview,
      api: ChatApi = fromEnvironment(::ChatApi),
      conversation: Conversation = Conversation()
    ): A = chat(Prompt(CustomModel(model.value), prompt), target, api, conversation)

    @AiDsl
    suspend inline operator fun <reified A : Any> invoke(
      prompt: Prompt<CreateChatCompletionRequestModel>,
      target: KType = typeOf<A>(),
      api: ChatApi = fromEnvironment(::ChatApi),
      conversation: Conversation = Conversation()
    ): A = chat(prompt, target, api, conversation)

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    @AiDsl
    suspend inline fun <reified A : Any> chat(
      prompt: Prompt<CreateChatCompletionRequestModel>,
      target: KType = typeOf<A>(),
      api: ChatApi = fromEnvironment(::ChatApi),
      conversation: Conversation = Conversation()
    ): A {
      val kind =
        (target.classifier as? KClass<*>)?.serializer()?.descriptor?.kind
          ?: error("Cannot find SerialKind for $target")
      return when (kind) {
        SerialKind.ENUM -> invokeEnum<A>(prompt, target, api, conversation)
        else -> {
          chat(
              target = target,
              model = prompt.model,
              api = api,
              conversation = conversation,
              enumSerializer = null,
              caseSerializers = emptyList()
            ) {
              serializer<A>()
            }
            .invoke(prompt)
        }
      }
    }
  }
}
