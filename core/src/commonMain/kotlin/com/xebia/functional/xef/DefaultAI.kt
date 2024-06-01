package com.xebia.functional.xef

import ai.xef.Chat
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.CreateChatCompletionRequest
import com.xebia.functional.xef.llm.StreamedFunction
import com.xebia.functional.xef.llm.prompt
import com.xebia.functional.xef.llm.promptStreaming
import com.xebia.functional.xef.prompt.Prompt
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

abstract class DefaultAI<A: Any>(
  private val target: KType,
  val model: Chat,
  val serializer: () -> KSerializer<A>,
  private val enumSerializer: ((case: String) -> A)?,
) : AI<A> {

  @Serializable data class Value<A>(val value: A)

  private suspend fun <B> runWithSerializer(prompt: Prompt, serializer: KSerializer<B>, conversation: Conversation): B =
    model.prompt(prompt, conversation, serializer)

  private fun runStreamingWithStringSerializer(prompt: Prompt, conversation: Conversation): Flow<String> =
    model.promptStreaming(prompt, conversation)

  private fun <B> runStreamingWithFunctionSerializer(
    prompt: Prompt,
    serializer: KSerializer<B>,
    conversation: Conversation
  ): Flow<StreamedFunction<B>> = model.promptStreaming(prompt, conversation, serializer)

  private suspend fun <B> runWithDescriptors(
    prompt: Prompt,
    serializer: KSerializer<B>,
    descriptors: List<SerialDescriptor>,
    conversation: Conversation
  ): B = model.prompt(prompt, conversation, serializer, descriptors)

  @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
  override suspend operator fun invoke(prompt: Prompt, conversation: Conversation): A {
    val serializer = serializer()
    return when (serializer.descriptor.kind) {
      SerialKind.ENUM -> {
        runWithEnumSingleTokenSerializer(serializer, prompt)
      }
      // else -> runWithSerializer(prompt, serializer)
      PolymorphicKind.OPEN ->
        when {
          target == typeOf<Flow<String>>() -> {
            runStreamingWithStringSerializer(prompt, conversation) as A
          }
          (target.classifier == Flow::class &&
            target.arguments.firstOrNull()?.type?.classifier == StreamedFunction::class) -> {
            val functionClass =
              target.arguments.first().type?.arguments?.firstOrNull()?.type?.classifier
                as? KClass<*>
            val functionSerializer =
              functionClass?.serializer() ?: error("Cannot find serializer for $functionClass")
            runStreamingWithFunctionSerializer(prompt, functionSerializer, conversation) as A
          }
          else -> {
            runWithSerializer(prompt, Value.serializer(serializer), conversation) as A
          }
        }
      PolymorphicKind.SEALED -> {
        val s = serializer as SealedClassSerializer<A>
        val cases = s.descriptor.elementDescriptors.toList()[1].elementDescriptors.toList()
        runWithDescriptors(prompt, s, cases, conversation)
      }
      SerialKind.CONTEXTUAL -> runWithSerializer(prompt, serializer, conversation)
      StructureKind.CLASS -> runWithSerializer(prompt, serializer, conversation)
      else -> runWithSerializer(prompt, Value.serializer(serializer), conversation).value
    }
  }

  @OptIn(ExperimentalSerializationApi::class)
  suspend fun runWithEnumSingleTokenSerializer(serializer: KSerializer<A>, prompt: Prompt): A {
    val cases =
      serializer.descriptor.elementDescriptors.map { it.serialName.substringAfterLast(".") }
    val logitBias =
      cases
        .flatMap {
          val result = model.tokenizer.encode(it)
          if (result.size > 1) {
            error("Cannot encode enum case $it into one token")
          }
          result
        }
        .associate { "$it" to 100 }
    val result =
      model.createChatCompletion(
        CreateChatCompletionRequest(
          messages = prompt.messages,
          model = model,
          logitBias = logitBias,
          maxTokens = 1,
          temperature = 0.0,
          n = 1,
          tools = emptyList(),
          toolChoice = null,
          user = prompt.configuration.user,
          seed = prompt.configuration.seed,
          stream = false
        )
      )
    val choice = result.choices[0].message.content
    val enumSerializer = enumSerializer
    return if (choice != null && enumSerializer != null) {
      enumSerializer(choice)
    } else {
      error("Cannot decode enum case from $choice")
    }
  }
}
