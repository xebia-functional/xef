package com.xebia.functional.xef.llm

import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.streaming.FunctionCallFormat
import com.xebia.functional.xef.llm.streaming.JsonSupport
import com.xebia.functional.xef.llm.streaming.XmlSupport
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.user
import com.xebia.functional.xef.prompt.ToolCallStrategy
import io.ktor.client.request.*
import kotlin.jvm.JvmSynthetic
import kotlinx.coroutines.flow.*

sealed class StreamedFunction<out A> {
  data class Property(val path: List<String>, val name: String, val value: String) :
    StreamedFunction<Nothing>()

  data class Result<out A>(val value: A) : StreamedFunction<A>()

  fun print() {
    when (this) {
      is Property -> println("Property: $name = $value")
      is Result -> println("Result: $value")
    }
  }

  companion object {

    /**
     * Streams function calls and results to a FlowCollector of StreamedFunction. It collects
     * responses from a ChatWithFunctions and creates chat completions. It also manages memory and
     * properties required for the function call.
     *
     * As the response is getting streamed from the model this function parses the incoming
     * potentially malformed json searching for a complete property that can be streamed back to the
     * user before the final result for the function call is ready.
     *
     * @param chat the ChatWithFunctions object representing the chat.
     * @param promptMessages prompt messages that are added to conversation history if request
     *   successful.
     * @param request the ChatCompletionRequest object representing the completion request.
     * @param scope the Conversation object representing the conversation scope.
     * @param serializer the function used to deserialize JSON strings into objects of type A.
     * @param A the type of the objects to be deserialized from JSON strings.
     */
    @JvmSynthetic
    internal suspend fun <A> FlowCollector<StreamedFunction<A>>.streamFunctionCall(
      chat: Chat,
      prompt: Prompt,
      request: CreateChatCompletionRequest,
      scope: Conversation,
      serializer: (output: String) -> A,
      function: FunctionObject
    ) {
      val messages = mutableListOf<ChatCompletionRequestMessage>()
      // this function call is mutable and will be updated as the stream progresses
      var functionCall = ChatCompletionMessageToolCallFunction("", "")
      // the current property is mutable and will be updated as the stream progresses
      var currentProperty: String? = null
      // we keep track to not emit the same property multiple times
      val streamedProperties = mutableSetOf<Property>()
      // the path to this potential nested property
      var path: List<String> = emptyList()
      // we extract the expected JSON schema before the LLM replies
      val schema = function.parameters
      // we create an example from the schema from which we can expect and infer the paths
      // as the LLM is sending us chunks with malformed JSON or XML
      // val format : FunctionCallFormat = JsonSupport
      if (schema != null) {
        val format = functionCallFormat(prompt)
        val stream = functionCallStream(prompt, chat, request)
        val example = format.createExampleFromSchema(schema)
        stream
          .onCompletion { addMessagesToMemory(prompt, messages, scope) }
          .collect { responseChunk ->
            // Each chunk is emitted from the LLM and it will include a delta.parameters with
            // the function is streaming, the JSON received will be partial and usually malformed
            // and needs to be inspected and clean up to stream properties before
            // the final result is ready

            // every response chunk contains a list of choices
            if (responseChunk.choices.isNotEmpty()) {
              // the delta contains the last emission while emitting the json character by character
              val delta = responseChunk.choices.first().delta
              // at any point the delta may be the last one
              val finishReason = responseChunk.choices.first().finishReason
              val toolCalls = delta.toolCalls.orEmpty()
              toolCalls.forEach { toolCall ->
                val fn = toolCall.function
                val functionName = fn?.name
                val arguments = fn?.arguments.orEmpty()
                if (functionName != null)
                // update the function name with the latest one
                functionCall = functionCall.copy(name = functionName)
                if (arguments.isNotEmpty()) {
                  // update the function arguments with the latest ones
                  functionCall = mergeArgumentsWithDelta(functionCall, toolCall)
                  // once we have info about the args we detect the last property referenced
                  // while streaming the arguments for the function call
                  val currentArg = getLastReferencedPropertyInArguments(functionCall)
                  if (currentProperty != currentArg && currentArg != null) {
                    // if the current property is different than the last one
                    // we update the path
                    // a change of property happens and we try to stream it
                    streamProperty(
                      format,
                      path,
                      currentProperty,
                      functionCall.arguments,
                      streamedProperties
                    )
                    path = format.findPropertyPath(example, currentArg) ?: listOf(currentArg)
                  }
                  // update the current property being evaluated
                  currentProperty = currentArg
                }
                if (finishReason != null) {
                  // the stream is finished and we try to stream the last property
                  // because the previous chunk may had a partial property whose body
                  // may had not been fully streamed
                  streamProperty(
                    format,
                    path,
                    currentProperty,
                    functionCall.arguments,
                    streamedProperties
                  )
                }
              }
              if (finishReason != null) {
                // we stream the result
                streamResult(format, functionCall, messages, serializer)
              }
            }
          }
      }
    }

    private suspend fun addMessagesToMemory(
      prompt: Prompt,
      messages: MutableList<ChatCompletionRequestMessage>,
      scope: Conversation
    ) {
      val newMessages = prompt.messages + messages
      newMessages.addToMemory(scope, prompt.configuration.messagePolicy.addMessagesToConversation)
    }

    private fun functionCallStream(
      prompt: Prompt,
      chat: Chat,
      request: CreateChatCompletionRequest
    ): Flow<CreateChatCompletionStreamResponse> =
      when (prompt.toolCallStrategy) {
        ToolCallStrategy.Supported -> chat.createChatCompletionStream(request)
        ToolCallStrategy.InferJsonFromStringResponse ->
          chat.createChatCompletionStreamFromStringParsing(JsonSupport, request)
        ToolCallStrategy.InferXmlFromStringResponse ->
          chat.createChatCompletionStreamFromStringParsing(XmlSupport, request)
      }

    private fun functionCallFormat(prompt: Prompt): FunctionCallFormat =
      when (prompt.toolCallStrategy) {
        ToolCallStrategy.Supported -> JsonSupport
        ToolCallStrategy.InferJsonFromStringResponse -> JsonSupport
        ToolCallStrategy.InferXmlFromStringResponse -> XmlSupport
      }

    private suspend fun <A> FlowCollector<StreamedFunction<A>>.streamResult(
      format: FunctionCallFormat,
      functionCall: ChatCompletionMessageToolCallFunction,
      messages: MutableList<ChatCompletionRequestMessage>,
      serializer: (output: String) -> A
    ) {
      val arguments = format.cleanArguments(functionCall)
      val jsonArguments = format.argumentsToJsonString(arguments)
      messages.add(PromptBuilder.assistant("Function call: $functionCall"))
      val result = serializer(jsonArguments)
      // stream the result
      emit(Result(result))
    }

    /**
     * Streams a property
     *
     * [currentArgs] may have malformed JSON, so we try to extract the body of the property and
     * repack it as a valid JSON string to be able to parse it as a JsonElement.
     *
     * If we are able to parse it that means we can stream the property.
     *
     * @param prop The name of the property to stream.
     * @param currentArgs The arguments containing the property value.
     * @param streamedProperties The set of already streamed properties.
     */
    private suspend fun <A> FlowCollector<StreamedFunction<A>>.streamProperty(
      format: FunctionCallFormat,
      path: List<String>,
      prop: String?,
      currentArgs: String?,
      streamedProperties: MutableSet<Property>
    ) {
      if (prop != null && currentArgs != null) {
        // stream a new property
        try {
          val propertyValue = format.propertyValue(prop, currentArgs)
          if (propertyValue != null) {
            // we try to extract the text value of the property
            // or for cases like objects that we don't want to report on
            // we return null
            val text = format.textProperty(propertyValue)
            if (text != null) {
              val streamedProperty = Property(path, prop, text)
              // we only stream the property if it has not been streamed before
              if (!streamedProperties.contains(streamedProperty)) {
                // stream the property
                emit(streamedProperty)
                streamedProperties.add(streamedProperty)
              }
            }
          }
        } catch (e: Throwable) {
          // ignore
        }
      }
    }

    private fun mergeArgumentsWithDelta(
      functionCall: ChatCompletionMessageToolCallFunction,
      functionCall0: ChatCompletionMessageToolCallChunk
    ): ChatCompletionMessageToolCallFunction =
      functionCall.copy(arguments = functionCall.arguments + (functionCall0.function?.arguments))

    private fun getLastReferencedPropertyInArguments(
      functionCall: ChatCompletionMessageToolCallFunction
    ): String? =
      """"(.*?)":"""
        .toRegex()
        .findAll(functionCall.arguments)
        .lastOrNull()
        ?.groupValues
        ?.lastOrNull()

    fun Chat.createChatCompletionStreamFromStringParsing(
      format: FunctionCallFormat,
      request: CreateChatCompletionRequest
    ): Flow<CreateChatCompletionStreamResponse> {
      val choiceName =
        request.toolChoice?.let {
          when (it) {
            is ChatCompletionToolChoiceOption.CaseChatCompletionNamedToolChoice ->
              it.value.function.name
            else -> null
          }
        }
      val tools = request.tools.orEmpty()
      val additionalMessage =
        if (tools.isEmpty()) null
        else
          user(
            """
            <tools>
              ${chatCompletionsAvailableToolsInstructions(format, tools)}
            </tools>
            ${if (choiceName != null) "<tool_choice>$choiceName</tool_choice>" else ""}
          """
              .trimIndent()
          )
      val modifiedRequest =
        request.copy(
          toolChoice = null,
          tools = emptyList(),
          messages = listOfNotNull(additionalMessage) + request.messages,
          stop = format.stopOn()
        )
      return createChatCompletionStream(modifiedRequest).map { response ->
        response.copy(
          choices =
            response.choices.map { choice ->
              choice.copy(
                ChatCompletionStreamResponseDelta(
                  toolCalls =
                    listOf(
                      ChatCompletionMessageToolCallChunk(
                        index = 0,
                        function =
                          ChatCompletionMessageToolCallChunkFunction(
                            name = choiceName,
                            arguments = choice.delta.content
                          )
                      )
                    )
                )
              )
            }
        )
      }
    }

    fun chatCompletionsAvailableToolsInstructions(
      format: FunctionCallFormat,
      tools: List<ChatCompletionTool>
    ): String = tools.joinToString("\n") { tool -> format.chatCompletionToolInstructions(tool) }
  }
}
