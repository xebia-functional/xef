package com.xebia.functional.xef.agents

import arrow.core.getOrElse
import arrow.core.raise.catch
import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.api.OpenAI
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.openai.generated.model.FunctionObject
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.system
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.user
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import kotlin.coroutines.Continuation
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object Agents {

  inline fun <reified Agent : Any> agent(
    config: Config = Config(),
    openAI: OpenAI = OpenAI(config),
    chat: Chat = openAI.chat,
    model: CreateChatCompletionRequestModel = CreateChatCompletionRequestModel.gpt_4o,
    conversation: Conversation = Conversation(),
    defaultSystemMessage: String = "You are a helpful assistant",
    scope: CoroutineScope = CoroutineScope(SupervisorJob())
  ): Agent {
    val systemAnnotation: System? = extractAnnotation<System, Agent>().firstOrNull()
    val systemMessage = systemAnnotation?.let { systemMessage(it) } ?: defaultSystemMessage
    val systemToolsInterfaces = systemAnnotation?.tools?.map { it } ?: emptyList()
    return proxy<Agent>(systemToolsInterfaces) { method, cont ->
      val userMessages = userMessages(method.arguments)
      val tools = method.tools
      val prompt = createPrompt(model, systemMessage, userMessages, tools)
      runMethodInContinuation(scope, prompt, method, model, chat, conversation, cont)
    }
  }

  @OptIn(ExperimentalSerializationApi::class)
  @PublishedApi
  internal inline fun <reified A : Annotation, reified Target : Any> extractAnnotation(): List<A> {
    val descriptor = serialDescriptor<Target>()
    return descriptor.annotations.mapNotNull { it as? A }
  }

  @PublishedApi
  internal fun systemMessage(annotation: System): String {
    return annotation.instructions.joinToString("\n")
  }

  @PublishedApi
  internal fun runMethodInContinuation(
    scope: CoroutineScope,
    prompt: Prompt,
    methodCall: MethodCall,
    model: CreateChatCompletionRequestModel,
    chat: Chat,
    conversation: Conversation,
    cont: Continuation<Any?>
  ): Deferred<Any> = scope.async {
    try {
      executeSymbolicMethod(prompt, methodCall, model, chat, conversation, cont)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      cont.resumeWithException(e)
    }
  }

  @PublishedApi
  internal fun createPrompt(
    model: CreateChatCompletionRequestModel,
    systemMessage: String,
    userMessages: List<String>,
    tools: List<AvailableTool>
  ): Prompt = Prompt(
    model = model,
    functions = functionObjects(tools)
  ) {
    +system(systemMessage)
    userMessages.forEach { userMessage ->
      +user(userMessage)
    }
  }

  private fun functionObjects(tools: List<AvailableTool>): List<FunctionObject> =
    tools.map {
      FunctionObject(
        name = it.name,
        description = it.description,
        parameters = it.parameters
      )
    }

  @PublishedApi
  internal suspend fun executeSymbolicMethod(
    prompt: Prompt,
    methodCall: MethodCall,
    model: CreateChatCompletionRequestModel,
    chat: Chat,
    conversation: Conversation,
    cont: Continuation<Any?>
  ): Any = catch({
    val retType = methodCall.returnType
    val ai = AI.chat(
      target = retType,
      model = model,
      api = chat,
      conversation = conversation,
      enumSerializer = null,
      caseSerializers = emptyList(),
      serializer = {
        serializer(retType) as KSerializer<Any>
      },
    )
    val result = ai.invoke(prompt)
    cont.resume(result)
  }) { exception ->
    cont.resumeWithException(exception)
  }

  @PublishedApi
  internal fun userMessages(args: List<Argument>): List<String> = args.map { arg ->
    val userAnnotationMessages = arg.annotations.filterIsInstance<User>().flatMap {
      it.prompt.toList()
    }
    if (userAnnotationMessages.isNotEmpty()) {
      userAnnotationMessages.joinToString {
        replaceUserPromptTemplateParameter(arg, it)
      }
    } else {
      arg.value.toString()
    }
  }

  @OptIn(InternalSerializationApi::class)
  private fun replaceUserPromptTemplateParameter(arg: Argument, it: String): String =
    try {
      val serializer = arg.type.serializer() as KSerializer<Any?>
      val json = Config.DEFAULT.json
      val jsonRpr = json.encodeToJsonElement(serializer, arg.value)
      val strReplacement = when (jsonRpr) {
        is JsonArray -> json.encodeToString(JsonArray.serializer(), jsonRpr)
        is JsonObject -> json.encodeToString(JsonObject.serializer(), jsonRpr)
        is JsonPrimitive -> jsonRpr.content
        JsonNull -> "null"
        else -> jsonRpr.toString()
      }
      it.replace("{{${arg.name}}}", strReplacement)
    } catch (e: Exception) {
      it.replace("{{${arg.name}}}", arg.value.toString())
    }

}
