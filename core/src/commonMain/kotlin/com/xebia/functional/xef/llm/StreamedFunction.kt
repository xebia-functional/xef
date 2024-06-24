package com.xebia.functional.xef.llm

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.StreamedFunction.Companion.PropertyType.*
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder
import io.github.nomisrev.openapi.*
import kotlin.jvm.JvmSynthetic
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.onCompletion
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*

sealed class StreamedFunction<out A> {
  data class Property(val path: List<String>, val name: String, val value: String) :
    StreamedFunction<Nothing>()

  data class Result<out A>(val value: A) : StreamedFunction<A>()

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
      serializer: (json: String) -> A,
      function: FunctionObject
    ) {
      val messages = mutableListOf<ChatCompletionRequestMessage>()
      // this function call is mutable and will be updated as the stream progresses
      var functionCall = ChatCompletionMessageToolCall.Function("", "")
      // the current property is mutable and will be updated as the stream progresses
      var currentProperty: String? = null
      // we keep track to not emit the same property multiple times
      val streamedProperties = mutableSetOf<Property>()
      // the path to this potential nested property
      var path: List<String> = emptyList()
      // we extract the expected JSON schema before the LLM replies
      val schema = function.parameters
      // we create an example from the schema from which we can expect and infer the paths
      // as the LLM is sending us chunks with malformed JSON
      if (schema != null) {
        val example = createExampleFromSchema(schema)
        chat.completions
          .createChatCompletionStream(request)
          .onCompletion {
            val newMessages = prompt.messages + messages
            newMessages.addToMemory(
              scope,
              prompt.configuration.messagePolicy.addMessagesToConversation
            )
          }
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
                      path,
                      currentProperty,
                      functionCall.arguments,
                      streamedProperties
                    )
                    path = findPropertyPath(example, currentArg) ?: listOf(currentArg)
                  }
                  // update the current property being evaluated
                  currentProperty = currentArg
                }
                if (finishReason != null) {
                  // the stream is finished and we try to stream the last property
                  // because the previous chunk may had a partial property whose body
                  // may had not been fully streamed
                  streamProperty(path, currentProperty, functionCall.arguments, streamedProperties)
                }
              }
              if (finishReason != null) {
                // we stream the result
                streamResult(functionCall, messages, serializer)
              }
            }
          }
      }
    }

    private suspend fun <A> FlowCollector<StreamedFunction<A>>.streamResult(
      functionCall: ChatCompletionMessageToolCall.Function,
      messages: MutableList<ChatCompletionRequestMessage>,
      serializer: (json: String) -> A
    ) {
      val arguments = functionCall.arguments
      messages.add(PromptBuilder.assistant("Function call: $functionCall"))
      val result = serializer(arguments)
      // stream the result
      emit(Result(result))
    }

    /**
     * The PropertyType enum represents the different types of properties that can be identified
     * from JSON. These include STRING, NUMBER, BOOLEAN, ARRAY, OBJECT, NULL, and UNKNOWN.
     *
     * STRING: Represents a property with a string value. NUMBER: Represents a property with a
     * numeric value. BOOLEAN: Represents a property with a boolean value. ARRAY: Represents a
     * property that is an array of values. OBJECT: Represents a property that is an object with
     * key-value pairs. NULL: Represents a property with a null value. UNKNOWN: Represents a
     * property of unknown type.
     */
    private enum class PropertyType {
      STRING,
      NUMBER,
      BOOLEAN,
      ARRAY,
      OBJECT,
      NULL,
      UNKNOWN
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
      path: List<String>,
      prop: String?,
      currentArgs: String?,
      streamedProperties: MutableSet<Property>
    ) {
      if (prop != null && currentArgs != null) {
        // stream a new property
        try {
          val remainingText = currentArgs.replace("\n", "")
          val body = remainingText.substringAfterLast("\"$prop\":").trim()
          // detect the type of the property
          val propertyType = propertyType(body)
          // extract the body of the property or if null don't report it
          val detectedBody = extractBody(propertyType, body) ?: return
          // repack the body as a valid JSON string
          val propertyValueAsJson = repackBodyAsJsonString(propertyType, detectedBody)
          if (propertyValueAsJson != null) {
            val propertyValue = Json.decodeFromString<JsonElement>(propertyValueAsJson)
            // we try to extract the text value of the property
            // or for cases like objects that we don't want to report on
            // we return null
            val text = textProperty(propertyValue)
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

    /**
     * Repacks the detected body as a JSON string based on the provided property type.
     *
     * @param propertyType The property type to determine how the body should be repacked.
     * @param detectedBody The detected body to be repacked as a JSON string.
     * @return The repacked body as a JSON string.
     */
    private fun repackBodyAsJsonString(propertyType: PropertyType, detectedBody: String?): String? =
      when (propertyType) {
        STRING -> "\"$detectedBody\""
        NUMBER -> detectedBody
        BOOLEAN -> detectedBody
        ARRAY -> "[$detectedBody]"
        OBJECT -> "{$detectedBody}"
        NULL -> "null"
        else -> null
      }

    /**
     * Extracts the body from a given input string which may contain potentially malformed json or
     * partial json chunk results.
     *
     * @param propertyType The type of property being extracted.
     * @param body The input string to extract the body from.
     * @return The extracted body string, or null if the body cannot be found.
     */
    private fun extractBody(propertyType: PropertyType, body: String): String? =
      when (propertyType) {
        STRING -> stringBody.find(body)?.groupValues?.get(1)
        NUMBER -> numberBody.find(body)?.groupValues?.get(1)
        BOOLEAN -> booleanBody.find(body)?.groupValues?.get(1)
        ARRAY -> arrayBody.find(body)?.groupValues?.get(1)
        OBJECT -> objectBody.find(body)?.groupValues?.get(1)
        NULL -> nullBody.find(body)?.groupValues?.get(1)
        else -> null
      }

    /**
     * Determines the type of a property based on a partial chnk of it's body.
     *
     * @param body The body of the property.
     * @return The type of the property.
     */
    private fun propertyType(body: String): PropertyType =
      when (body.firstOrNull()) {
        '"' -> STRING
        in '0'..'9' -> NUMBER
        't',
        'f' -> BOOLEAN
        '[' -> ARRAY
        '{' -> OBJECT
        'n' -> NULL
        else -> UNKNOWN
      }

    private val stringBody = """\"(.*?)\"""".toRegex()
    private val numberBody = "(-?\\d+(\\.\\d+)?)".toRegex()
    private val booleanBody = """(true|false)""".toRegex()
    private val arrayBody = """\[(.*?)\]""".toRegex()
    private val objectBody = """\{(.*?)\}""".toRegex()
    private val nullBody = """null""".toRegex()

    /**
     * Searches for the content of the property within a given JsonElement.
     *
     * @param element The JsonElement to search within.
     * @return The text property as a String, or null if not found.
     */
    private fun textProperty(element: JsonElement): String? {
      return when (element) {
        // we don't report on properties holding objects since we report on the properties of the
        // object
        is JsonObject -> null
        is JsonArray -> element.map { textProperty(it) }.joinToString(", ")
        is JsonPrimitive -> element.content
        is JsonNull -> "null"
      }
    }

    private fun mergeArgumentsWithDelta(
      functionCall: ChatCompletionMessageToolCall.Function,
      functionCall0: ChatCompletionMessageToolCallChunk
    ): ChatCompletionMessageToolCall.Function =
      functionCall.copy(arguments = functionCall.arguments + (functionCall0.function?.arguments))

    private fun getLastReferencedPropertyInArguments(
      functionCall: ChatCompletionMessageToolCall.Function
    ): String? =
      """"(.*?)":"""
        .toRegex()
        .findAll(functionCall.arguments)
        .lastOrNull()
        ?.groupValues
        ?.lastOrNull()

    private fun findPropertyPath(jsonElement: JsonElement, targetProperty: String): List<String>? {
      return findPropertyPathTailrec(listOf(jsonElement to emptyList()), targetProperty)
    }

    private tailrec fun findPropertyPathTailrec(
      stack: List<Pair<JsonElement, List<String>>>,
      targetProperty: String
    ): List<String>? {
      if (stack.isEmpty()) return null

      val (currentElement, currentPath) = stack.first()
      val remainingStack = stack.drop(1)

      return when (currentElement) {
        is JsonObject -> {
          if (currentElement.containsKey(targetProperty)) {
            currentPath + targetProperty
          } else {
            val newStack = currentElement.entries.map { it.value to (currentPath + it.key) }
            findPropertyPathTailrec(remainingStack + newStack, targetProperty)
          }
        }
        is JsonArray -> {
          val newStack = currentElement.map { it to currentPath }
          findPropertyPathTailrec(remainingStack + newStack, targetProperty)
        }
        else -> findPropertyPathTailrec(remainingStack, targetProperty)
      }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun createExampleFromSchema(jsonElement: JsonElement): JsonElement {
      return when {
        jsonElement is JsonObject && jsonElement.containsKey("type") -> {
          when (jsonElement["type"]?.jsonPrimitive?.content) {
            "object" -> {
              val properties = jsonElement["properties"] as? JsonObject
              val resultMap = mutableMapOf<String, JsonElement>()
              properties?.forEach { (key, value) ->
                resultMap[key] = createExampleFromSchema(value)
              }
              JsonObject(resultMap)
            }
            "array" -> {
              val items = jsonElement["items"]
              val exampleItems = items?.let { createExampleFromSchema(it) }
              JsonArray(listOfNotNull(exampleItems))
            }
            "string" -> JsonPrimitive("default_string")
            "number" -> JsonPrimitive(0)
            else -> JsonPrimitive(null)
          }
        }
        else -> JsonPrimitive(null)
      }
    }
  }
}
