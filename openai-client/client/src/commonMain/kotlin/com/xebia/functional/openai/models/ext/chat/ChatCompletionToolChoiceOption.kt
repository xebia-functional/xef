package com.xebia.functional.openai.models.ext.chat

import com.xebia.functional.openai.models.ChatCompletionNamedToolChoiceFunction
import kotlin.jvm.JvmInline
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Controls which (if any) function is called by the model. `none` means the model will not call a
 * function and instead generates a message. `auto` means the model can pick between generating a
 * message or calling a function. Specifying a particular function via `{\"type: \"function\",
 * \"function\": {\"name\": \"my_function\"}}` forces the model to call that function. `none` is the
 * default when no functions are present. `auto` is the default if functions are present.
 *
 * @param type The type of the tool. Currently, only `function` is supported.
 * @param function
 */
@JvmInline
@Serializable
value class ChatCompletionToolChoiceOption(val element: JsonElement) {

  companion object {
    val none = ChatCompletionToolChoiceOption(JsonPrimitive("none"))
    val auto = ChatCompletionToolChoiceOption(JsonPrimitive("auto"))

    fun function(function: ChatCompletionNamedToolChoiceFunction) =
      ChatCompletionToolChoiceOption(
        JsonObject(
          mapOf(
            "type" to JsonPrimitive("function"),
            "function" to
              Json.encodeToJsonElement(ChatCompletionNamedToolChoiceFunction.serializer(), function)
          )
        )
      )
  }
}
