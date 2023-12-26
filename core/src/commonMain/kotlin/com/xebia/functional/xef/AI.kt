package com.xebia.functional.xef

import ai.xef.openai.StandardModel
import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.models.CreateChatCompletionRequest
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestUserMessage
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestUserMessageContentText
import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.StreamedFunction
import com.xebia.functional.xef.llm.fromEnvironment
import com.xebia.functional.xef.llm.models.modelType
import com.xebia.functional.xef.llm.prompt
import com.xebia.functional.xef.llm.promptStreaming
import com.xebia.functional.xef.prompt.Prompt
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*

interface AI<A : Any> {
  val target: KType
  val model: CreateChatCompletionRequestModel
  val api: ChatApi
  val serializer: () -> KSerializer<A>
  val conversation: Conversation
  val enumSerializer: ((case: String) -> A)?
  val caseSerializers: List<KSerializer<A>>

  @Serializable data class Value<A>(val value: A)

  private suspend fun <B> runWithSerializer(prompt: String, serializer: KSerializer<B>): B =
    api.prompt(Prompt(StandardModel(model), prompt), conversation, serializer)

  private fun runStreamingWithStringSerializer(prompt: String): Flow<String> =
    api.promptStreaming(Prompt(StandardModel(model), prompt), conversation)

  private fun <B> runStreamingWithFunctionSerializer(
    prompt: String,
    serializer: KSerializer<B>
  ): Flow<StreamedFunction<B>> =
    api.promptStreaming(Prompt(StandardModel(model), prompt), conversation, serializer)

  private suspend fun <B> runWithDescriptors(
    prompt: String,
    serializer: KSerializer<B>,
    descriptors: List<SerialDescriptor>
  ): B = api.prompt(Prompt(StandardModel(model), prompt), conversation, serializer, descriptors)

  @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
  suspend operator fun invoke(prompt: String): A {
    val serializer = serializer()
    return when (serializer.descriptor.kind) {
      SerialKind.ENUM -> {
        runWithEnumSingleTokenSerializer(serializer, prompt)
      }
      // else -> runWithSerializer(prompt, serializer)
      PolymorphicKind.OPEN ->
        when {
          target == typeOf<Flow<String>>() -> {
            runStreamingWithStringSerializer(prompt) as A
          }
          (target.classifier == Flow::class &&
            target.arguments.firstOrNull()?.type?.classifier == StreamedFunction::class) -> {
            val functionClass =
              target.arguments.first().type?.arguments?.firstOrNull()?.type?.classifier
                as? KClass<*>
            val functionSerializer =
              functionClass?.serializer() ?: error("Cannot find serializer for $functionClass")
            runStreamingWithFunctionSerializer(prompt, functionSerializer) as A
          }
          else -> {
            runWithSerializer(prompt, Value.serializer(serializer)) as A
          }
        }
      PolymorphicKind.SEALED -> {
        val s = serializer as SealedClassSerializer<A>
        val cases = s.descriptor.elementDescriptors.toList()[1].elementDescriptors.toList()
        runWithDescriptors(prompt, s, cases)
      }
      SerialKind.CONTEXTUAL -> runWithSerializer(prompt, serializer)
      StructureKind.CLASS -> runWithSerializer(prompt, serializer)
      else -> runWithSerializer(prompt, Value.serializer(serializer)).value
    }
  }

  @OptIn(ExperimentalSerializationApi::class)
  suspend fun runWithEnumSingleTokenSerializer(serializer: KSerializer<A>, prompt: String): A {
    val encoding = StandardModel(model).modelType(forFunctions = false).encoding
    val cases =
      serializer.descriptor.elementDescriptors.map { it.serialName.substringAfterLast(".") }
    val logitBias =
      cases
        .flatMap {
          val result = encoding.encode(it)
          if (result.size > 1) {
            error("Cannot encode enum case $it into one token")
          }
          result
        }
        .associate { "$it" to 100 }
    val result =
      api.createChatCompletion(
        CreateChatCompletionRequest(
          messages =
            listOf(
              ChatCompletionRequestUserMessage(
                content = listOf(ChatCompletionRequestUserMessageContentText(prompt)),
              )
            ),
          model = StandardModel(model),
          logitBias = logitBias,
          maxTokens = 1,
          temperature = 0.0
        )
      )
    val choice = result.body().choices[0].message.content
    val enumSerializer = enumSerializer
    return if (choice != null && enumSerializer != null) {
      enumSerializer(choice)
    } else {
      error("Cannot decode enum case from $choice")
    }
  }

  companion object {
    operator fun <A : Any> invoke(
      target: KType,
      model: CreateChatCompletionRequestModel,
      api: ChatApi,
      conversation: Conversation,
      enumSerializer: ((case: String) -> A)?,
      caseSerializers: List<KSerializer<A>>,
      serializer: () -> KSerializer<A>,
    ): AI<A> =
      object : AI<A> {
        override val target: KType = target
        override val model: CreateChatCompletionRequestModel = model
        override val api: ChatApi = api
        override val serializer: () -> KSerializer<A> = serializer
        override val conversation: Conversation = conversation
        override val enumSerializer: ((case: String) -> A)? = enumSerializer
        override val caseSerializers: List<KSerializer<A>> = caseSerializers
      }

    @OptIn(InternalSerializationApi::class)
    @PublishedApi
    internal suspend inline fun <reified A : Any> invokeEnum(
      prompt: String,
      target: KType = typeOf<A>(),
      model: CreateChatCompletionRequestModel = CreateChatCompletionRequestModel.gpt_4_1106_preview,
      api: ChatApi = fromEnvironment(::ChatApi),
      conversation: Conversation = Conversation()
    ): A =
      invoke(
          target = target,
          model = model,
          api = api,
          conversation = conversation,
          enumSerializer = { @Suppress("UPPER_BOUND_VIOLATED") enumValueOf<A>(it) },
          caseSerializers = emptyList()
        ) {
          serializer<A>()
        }
        .invoke(prompt)

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    @AiDsl
    suspend inline operator fun <reified A : Any> invoke(
      prompt: String,
      target: KType = typeOf<A>(),
      model: CreateChatCompletionRequestModel = CreateChatCompletionRequestModel.gpt_4_1106_preview,
      api: ChatApi = fromEnvironment(::ChatApi),
      conversation: Conversation = Conversation()
    ): A {
      val kind =
        (target.classifier as? KClass<*>)?.serializer()?.descriptor?.kind
          ?: error("Cannot find SerialKind for $target")
      return when (kind) {
        SerialKind.ENUM -> invokeEnum<A>(prompt, target, model, api, conversation)
        else -> {
          invoke(
              target = target,
              model = model,
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
