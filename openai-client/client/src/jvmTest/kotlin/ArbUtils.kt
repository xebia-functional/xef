package com.xebia.functional

import com.xebia.functional.openai.models.ChatCompletionMessageToolCall
import com.xebia.functional.openai.models.ChatCompletionMessageToolCallFunction
import com.xebia.functional.openai.models.FunctionObject
import com.xebia.functional.openai.models.ext.chat.*
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

val simpleStringArb = Arb.stringPattern("[A-Za-z0-9]+")
val idArb = Arb.uuid().map { it.toString() }

val jsonStringArb: Arb<JsonElement> = simpleStringArb.map { JsonPrimitive(it) }
val jsonIntArb: Arb<JsonElement> = Arb.int().map { JsonPrimitive(it) }
val jsonBooleanArb: Arb<JsonElement> = Arb.boolean().map { JsonPrimitive(it) }

val jsonObjectFieldArb: Arb<Pair<String, JsonElement>> = arbitrary {
  val key = simpleStringArb.bind()
  val value = Arb.choice(jsonStringArb, jsonIntArb, jsonBooleanArb).bind()
  key to value
}

val functionObjectArb = arbitrary {
  val name = idArb.bind()
  val fields = Arb.list(jsonObjectFieldArb).map { JsonObject(it.toMap()) }.bind()
  val description = Arb.string().orNull(0.5).bind()
  FunctionObject(name, description, fields)
}

val contentImageUrlDetailArb =
  Arb.element(
    ChatCompletionRequestUserMessageContentImageUrl.Detail.auto,
    ChatCompletionRequestUserMessageContentImageUrl.Detail.high,
    ChatCompletionRequestUserMessageContentImageUrl.Detail.low
  )

val contentImageUrlArb = arbitrary {
  val url = simpleStringArb.orNull(0.5).bind()
  val detail = contentImageUrlDetailArb.orNull(0.5).bind()
  ChatCompletionRequestUserMessageContentImageUrl(url, detail)
}

val chatCompletionMessageToolCallFunction = arbitrary {
  val name = simpleStringArb.bind()
  val args = simpleStringArb.bind()
  ChatCompletionMessageToolCallFunction(name, args)
}

val chatCompletionMessageToolCall = arbitrary {
  val id = idArb.bind()
  val func = chatCompletionMessageToolCallFunction.bind()
  ChatCompletionMessageToolCall(id, ChatCompletionMessageToolCall.Type.function, func)
}

val chatCompletionRequestAssistantMessage = arbitrary {
  val content = simpleStringArb.orNull(0.5).bind()
  val tools = Arb.list(chatCompletionMessageToolCall).bind()
  ChatCompletionRequestAssistantMessage(content, tools)
}

val chatCompletionRequestToolMessage = arbitrary {
  val content = simpleStringArb.orNull(0.5).bind()
  val toolCallId = idArb.bind()
  ChatCompletionRequestToolMessage(content, toolCallId)
}
