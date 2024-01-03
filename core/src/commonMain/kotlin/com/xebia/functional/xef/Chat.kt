package com.xebia.functional.xef

import ai.xef.openai.OpenAIModel
import ai.xef.openai.StandardModel
import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.models.CreateChatCompletionRequest
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.StreamedFunction
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

data class Chat<A : Any>(
  val target: KType,
  val model: OpenAIModel<CreateChatCompletionRequestModel>,
  val api: ChatApi,
  val serializer: () -> KSerializer<A>,
  val conversation: Conversation,
  val enumSerializer: ((case: String) -> A)?,
  val caseSerializers: List<KSerializer<A>>
) : AI {

  @Serializable data class Value<A>(val value: A)

  private suspend fun <B> runWithSerializer(
    prompt: Prompt<CreateChatCompletionRequestModel>,
    serializer: KSerializer<B>
  ): B = api.prompt(prompt, conversation, serializer)

  private fun runStreamingWithStringSerializer(
    prompt: Prompt<CreateChatCompletionRequestModel>
  ): Flow<String> = api.promptStreaming(prompt, conversation)

  private fun <B> runStreamingWithFunctionSerializer(
    prompt: Prompt<CreateChatCompletionRequestModel>,
    serializer: KSerializer<B>
  ): Flow<StreamedFunction<B>> = api.promptStreaming(prompt, conversation, serializer)

  private suspend fun <B> runWithDescriptors(
    prompt: Prompt<CreateChatCompletionRequestModel>,
    serializer: KSerializer<B>,
    descriptors: List<SerialDescriptor>
  ): B = api.prompt(prompt, conversation, serializer, descriptors)

  @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
  suspend operator fun invoke(prompt: Prompt<CreateChatCompletionRequestModel>): A {
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
  suspend fun runWithEnumSingleTokenSerializer(
    serializer: KSerializer<A>,
    prompt: Prompt<CreateChatCompletionRequestModel>
  ): A {
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
          messages = prompt.messages,
          model = model,
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
}
