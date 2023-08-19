package com.xebia.functional.xef.llm

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.StreamedFunction.Companion.PropertyType.*
import com.xebia.functional.xef.llm.models.chat.ChatCompletionRequest
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.functions.FunctionCall
import com.xebia.functional.xef.prompt.templates.assistant
import kotlin.jvm.JvmSynthetic
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.onCompletion
import kotlinx.serialization.json.*

sealed class StreamedFunction<out A> {
  data class Property(val path: String, val name: String, val value: String) :
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
     * @param request the ChatCompletionRequest object representing the completion request.
     * @param scope the Conversation object representing the conversation scope.
     * @param serializer the function used to deserialize JSON strings into objects of type A.
     * @param A the type of the objects to be deserialized from JSON strings.
     */
    @JvmSynthetic
    suspend fun <A> FlowCollector<StreamedFunction<A>>.streamFunctionCall(
      chat: ChatWithFunctions,
      request: ChatCompletionRequest,
      scope: Conversation,
      serializer: (json: String) -> A
    ) {
      val messages = mutableListOf<Message>()
      // this function call is mutable and will be updated as the stream progresses
      var functionCall = FunctionCall(null, null)
      // the current property is mutable and will be updated as the stream progresses
      var currentProperty: String? = null
      // we keep track to not emit the same property multiple times
      val streamedProperties = mutableSetOf<Property>()
      // the path to this potential nested property
      var path: String? = null
      chat
        .createChatCompletions(request)
        .onCompletion { MemoryManagement.addMemoriesAfterStream(chat, request, scope, messages) }
        .collect { responseChunk ->
          // every response chunk contains a list of choices
          if (responseChunk.choices.isNotEmpty()) {
            // the delta contains the last emission while emitting the json character by character
            val delta = responseChunk.choices.first().delta
            // at any point the delta may be the last one
            val finishReason = responseChunk.choices.first().finishReason
            if (delta?.functionCall != null) {
              if (delta.functionCall.name != null)
              // update the function name with the latest one
              functionCall = functionCall.copy(name = delta.functionCall.name)
              if (delta.functionCall.arguments != null) {
                // update the function arguments with the latest ones
                functionCall = mergeArgumentsWithDelta(functionCall, delta.functionCall)
                // once we have info about the args we detect the last property referenced
                // while streaming the arguments for the function call
                val currentArg = getLastReferencedPropertyInArguments(functionCall)
                if (currentProperty != currentArg && currentArg != null) {
                  // if the current property is different than the last one
                  // we update the path
                  // a change of property happens and we try to stream it
                  streamProperty(path, currentProperty, functionCall.arguments, streamedProperties)
                  val updatedPath = if (path == null) currentArg else "$path.$currentArg"
                  path = updatedPath
                }
                // update the current property being evaluated
                currentProperty = currentArg
              }
            }
            if (finishReason != null) {
              // the stream is finished and we try to stream the last property
              // because the previous chunk may had a partial property whose body
              // may had not been fully streamed
              val currentPath = if (path == null) currentProperty else path
              streamProperty(
                currentPath,
                currentProperty,
                functionCall.arguments,
                streamedProperties
              )

              // we stream the result
              streamResult(functionCall, messages, serializer)
            }
          }
        }
    }

    private suspend fun <A> FlowCollector<StreamedFunction<A>>.streamResult(
      functionCall: FunctionCall,
      messages: MutableList<Message>,
      serializer: (json: String) -> A
    ) {
      val arguments = functionCall.arguments ?: error("No arguments provided for function call")
      messages.add(assistant("Function call: $functionCall"))
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
      path: String?,
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
          // extract the body of the property
          val detectedBody = extractBody(propertyType, body)
          // repack the body as a valid JSON string
          val propertyValueAsJson = repackBodyAsJsonString(propertyType, detectedBody)
          if (propertyValueAsJson != null) {
            val propertyValue = Json.decodeFromString<JsonElement>(propertyValueAsJson)
            // we try to extract the text value of the property
            // or for cases like objects and array that we don't want to report on
            // we return null
            val text = textProperty(propertyValue)
            if (text != null && path != null) {
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
     * Determines the type of a property based on its body.
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
    private val numberBody = """(-?\d+\.\d+)?""".toRegex()
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
        is JsonArray -> null
        is JsonPrimitive -> element.content
        is JsonNull -> "null"
      }
    }

    private fun mergeArgumentsWithDelta(
      functionCall: FunctionCall,
      functionCall0: FunctionCall
    ): FunctionCall =
      functionCall.copy(arguments = (functionCall.arguments ?: "") + (functionCall0.arguments))

    private fun getLastReferencedPropertyInArguments(functionCall: FunctionCall): String? =
      """"(.*?)":"""
        .toRegex()
        .findAll(functionCall.arguments!!)
        .lastOrNull()
        ?.groupValues
        ?.lastOrNull()
  }
}
