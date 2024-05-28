package com.xebia.functional.xef

import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequest
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.StreamedFunction
import com.xebia.functional.xef.llm.models.modelType
import com.xebia.functional.xef.llm.prompt
import com.xebia.functional.xef.llm.promptStreaming
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.user
import com.xebia.functional.xef.prompt.contentAsString
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*

data class DefaultAI<A : Any>(
  val target: KType,
  val model: CreateChatCompletionRequestModel,
  val api: Chat,
  val serializer: () -> KSerializer<A>,
  val conversation: Conversation,
  val enumSerializer: ((case: String) -> A)?,
  val caseSerializers: List<KSerializer<A>>
) : AI {

  @Serializable data class Value<A>(val value: A)

  private suspend fun <B> runWithSerializer(prompt: Prompt, serializer: KSerializer<B>): B =
    api.prompt(prompt, conversation, serializer)

  private fun runStreamingWithStringSerializer(prompt: Prompt): Flow<String> =
    api.promptStreaming(prompt, conversation)

  private fun <B> runStreamingWithFunctionSerializer(
    prompt: Prompt,
    serializer: KSerializer<B>
  ): Flow<StreamedFunction<B>> = api.promptStreaming(prompt, conversation, serializer)

  private suspend fun <B> runWithDescriptors(
    prompt: Prompt,
    serializer: KSerializer<B>,
    descriptors: List<SerialDescriptor>
  ): B = api.prompt(prompt, conversation, serializer, descriptors)

  @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
  suspend operator fun invoke(prompt: Prompt): A {
    val serializer = serializer()
    return when (serializer.descriptor.kind) {
      SerialKind.ENUM -> {
        runWithEnumSerializer(serializer, prompt)
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

  private suspend fun runWithEnumSerializer(serializer: KSerializer<A>, prompt: Prompt): A =
    if (prompt.configuration.supportsLogitBias) {
      runWithEnumSingleTokenSerializer(serializer, prompt)
    } else {
      runWithEnumRegexResponseSerializer(serializer, prompt)
    }

  private suspend fun runWithEnumRegexResponseSerializer(
    serializer: KSerializer<A>,
    prompt: Prompt
  ): A {
    val cases = casesFromEnumSerializer(serializer)
    val classificationMessage =
      user(
        """
      <instructions>
        You are an AI, expected to classify the `context` into one of the `cases`:
        <context>
          ${prompt.messages.joinToString("\n") { it.contentAsString() }}
        <cases>  
          ${cases.map { s -> "<case>$s</case>" }.joinToString("\n")}
        </cases>
        Select the `case` corresponding to the `context`.
        IMPORTANT. Reply exclusively with the selected `case`.
      </instructions>
    """
          .trimIndent()
      )
    val result =
      api.createChatCompletion(
        CreateChatCompletionRequest(
          messages = prompt.messages + classificationMessage,
          model = model,
          maxTokens = prompt.configuration.maxTokens,
          temperature = 0.0
        )
      )
    val casesRegexes = cases.map { ".*$it.*" }
    val responseContent = result.choices[0].message.content ?: ""
    val choice =
      casesRegexes
        .zip(cases)
        .firstOrNull {
          Regex(it.first, RegexOption.IGNORE_CASE).containsMatchIn(responseContent.trim())
        }
        ?.second
    return serializeWithEnumSerializer(choice, enumSerializer)
  }

  private suspend fun runWithEnumSingleTokenSerializer(
    serializer: KSerializer<A>,
    prompt: Prompt
  ): A {
    val encoding = model.modelType(forFunctions = false).encoding
    val cases = casesFromEnumSerializer(serializer)
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
    val choice = result.choices[0].message.content
    return serializeWithEnumSerializer(choice, enumSerializer)
  }

  @OptIn(ExperimentalSerializationApi::class)
  private fun casesFromEnumSerializer(serializer: KSerializer<A>): List<String> =
    serializer.descriptor.elementDescriptors.map { it.serialName.substringAfterLast(".") }

  private fun serializeWithEnumSerializer(
    choice: String?,
    enumSerializer: ((case: String) -> A)?
  ): A {
    return if (choice != null && enumSerializer != null) {
      enumSerializer(choice)
    } else {
      error("Cannot decode enum case from $choice")
    }
  }
}
