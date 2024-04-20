package com.xebia.functional.xef

import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.api.Images
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.serialization.Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialKind
import kotlin.coroutines.cancellation.CancellationException

sealed interface AI {

  fun interface PromptClassifier {
    fun template(input: String, output: String, context: String): String
  }

  companion object {

    fun <A : Any> chat(
      model: CreateChatCompletionRequestModel,
      api: Chat,
      conversation: Conversation,
      enumSerializer: ((case: String) -> A)?,
      caseSerializers: List<Serializer<A>>,
      serializer: () -> Serializer<A>,
    ): DefaultAI<A> =
      DefaultAI(
        model = model,
        api = api,
        serializer = serializer,
        conversation = conversation,
        enumSerializer = enumSerializer,
        caseSerializers = caseSerializers
      )

    fun images(
      config: Config = Config(),
    ): Images = OpenAI(config).images

    @PublishedApi
    internal suspend inline fun <reified A : Any> invokeEnum(
      prompt: Prompt,
      config: Config = Config(),
      api: Chat = OpenAI(config).chat,
      conversation: Conversation = Conversation()
    ): A =
      chat(
          model = prompt.model,
          api = api,
          conversation = conversation,
          enumSerializer = { @Suppress("UPPER_BOUND_VIOLATED") enumValueOf<A>(it) },
          caseSerializers = emptyList()
        ) {
          Serializer()
        }
        .invoke(prompt)

    /**
     * Classify a prompt using a given enum.
     *
     * @param input The input to the model.
     * @param output The output to the model.
     * @param context The context to the model.
     * @param model The model to use.
     * @param target The target type to return.
     * @param api The chat API to use.
     * @param conversation The conversation to use.
     * @return The classified enum.
     * @throws IllegalArgumentException If no enum values are found.
     */
    @AiDsl
    @Throws(IllegalArgumentException::class, CancellationException::class)
    suspend inline fun <reified E> classify(
      input: String,
      output: String,
      context: String,
      model: CreateChatCompletionRequestModel = CreateChatCompletionRequestModel.gpt_4_1106_preview,
      config: Config = Config(),
      api: Chat = OpenAI(config).chat,
      conversation: Conversation = Conversation()
    ): E where E : PromptClassifier, E : Enum<E> {
      val value = enumValues<E>().firstOrNull() ?: error("No enum values found")
      return invoke(
        prompt = value.template(input, output, context),
        model = model,
        config = config,
        api = api,
        conversation = conversation
      )
    }

    @AiDsl
    suspend inline operator fun <reified A : Any> invoke(
      prompt: String,
      model: CreateChatCompletionRequestModel = CreateChatCompletionRequestModel.gpt_3_5_turbo_0125,
      config: Config = Config(),
      api: Chat = OpenAI(config).chat,
      conversation: Conversation = Conversation()
    ): A = chat(Prompt(model, prompt), config, api, conversation)

    @AiDsl
    suspend inline operator fun <reified A : Any> invoke(
      prompt: Prompt,
      config: Config = Config(),
      api: Chat = OpenAI(config).chat,
      conversation: Conversation = Conversation()
    ): A = chat(prompt, config, api, conversation)

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    @AiDsl
    suspend inline fun <reified A : Any> chat(
      prompt: Prompt,
      config: Config = Config(),
      api: Chat = OpenAI(config).chat,
      conversation: Conversation = Conversation()
    ): A {
      val serializer = Serializer<A>()
      return when (serializer.kind) {
        SerialKind.ENUM -> invokeEnum<A>(prompt, config, api, conversation)
        else -> {
          chat(
              model = prompt.model,
              api = api,
              conversation = conversation,
              enumSerializer = null,
              caseSerializers = emptyList()
            ) {
              serializer
            }
            .invoke(prompt)
        }
      }
    }
  }
}
