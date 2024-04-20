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
import com.xebia.functional.xef.serialization.Serializer
import com.xebia.functional.xef.serialization.serializerOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*

data class DefaultAI<A : Any>(
  val model: CreateChatCompletionRequestModel,
  val api: Chat,
  val serializer: () -> Serializer<A>,
  val conversation: Conversation,
  val enumSerializer: ((case: String) -> A)?,
  val caseSerializers: List<Serializer<A>>
) : AI {

  @Serializable data class Value<A>(val value: A) {
    companion object {
      fun <A> Serializer(serializer: Serializer<A>): Serializer<A> =
        object : Serializer<A> {
          override fun serialize(value: A): String = serializer.serialize(value)
          override fun deserialize(value: String): A = serializer.deserialize(value)
          override val name: String = serializer.name
          override val schema: String = serializer.schema
          override val kind: SerialKind = serializer.kind
          override fun elements(): List<Serializer<*>> = serializer.elements()
          override fun cases(): List<Serializer<*>> = serializer.cases()
          override val isNullable: Boolean = serializer.isNullable
          override val elementsCount: Int = serializer.elementsCount
          override fun annotations(): List<Annotation> = serializer.annotations()
          override fun elementNames(): List<String> = serializer.elementNames()
          override val isFlowOfString: Boolean = false
          override val isFlowOfFunction: Boolean = false
          override val flowOfFunctionSerializer: Serializer<*>? = null
        }
    }
  }

  private suspend fun <B> runWithSerializer(prompt: Prompt, serializer: Serializer<B>): B =
    api.prompt(prompt, conversation, serializer)

  private fun runStreamingWithStringSerializer(prompt: Prompt): Flow<String> =
    api.promptStreaming(prompt, conversation)

  private fun <B> runStreamingWithFunctionSerializer(
    prompt: Prompt,
    serializer: Serializer<B>
  ): Flow<StreamedFunction<B>> = api.promptStreaming(prompt, conversation, serializer)

  private suspend fun <B> runWithDescriptors(
    prompt: Prompt,
    serializer: Serializer<B>,
    descriptors: List<Serializer<*>>
  ): B = api.prompt(prompt, conversation, serializer, descriptors)

  @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
  suspend operator fun invoke(prompt: Prompt): A {
    val serializer = serializer()
    return when (serializer.kind) {
      SerialKind.ENUM -> {
        runWithEnumSingleTokenSerializer(serializer, prompt)
      }
      // else -> runWithSerializer(prompt, serializer)
      PolymorphicKind.OPEN -> {
        val maybeFunctionSerializer = serializer.flowOfFunctionSerializer
        when {
          serializer.isFlowOfString -> {
            runStreamingWithStringSerializer(prompt) as A
          }

          serializer.isFlowOfFunction && maybeFunctionSerializer != null -> {
            runStreamingWithFunctionSerializer(prompt, maybeFunctionSerializer) as A
          }

          else -> {
            runWithSerializer(prompt, Value.Serializer(serializer)) as A
          }
        }
      }
      PolymorphicKind.SEALED -> {
        runWithDescriptors(prompt, serializer, serializer.cases())
      }
      SerialKind.CONTEXTUAL -> runWithSerializer(prompt, serializer)
      StructureKind.CLASS -> runWithSerializer(prompt, serializer)
      else -> runWithSerializer(prompt, Value.Serializer(serializer))
    }
  }

  @OptIn(ExperimentalSerializationApi::class)
  suspend fun runWithEnumSingleTokenSerializer(serializer: Serializer<A>, prompt: Prompt): A {
    val encoding = model.modelType(forFunctions = false).encoding
    val cases =
      serializer.elements().map { it.name.substringAfterLast(".") }
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
    val enumSerializer = enumSerializer
    return if (choice != null && enumSerializer != null) {
      enumSerializer(choice)
    } else {
      error("Cannot decode enum case from $choice")
    }
  }
}
