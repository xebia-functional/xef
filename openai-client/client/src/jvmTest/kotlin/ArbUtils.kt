package com.xebia.functional

import com.xebia.functional.openai.models.FunctionObject
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestUserMessageContentImageUrl
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

val simpleStringArb = Arb.stringPattern("[A-Za-z0-9]+")

val jsonStringArb: Arb<JsonElement> = simpleStringArb.map { JsonPrimitive(it) }
val jsonIntArb: Arb<JsonElement> = Arb.int().map { JsonPrimitive(it) }
val jsonBooleanArb: Arb<JsonElement> = Arb.boolean().map { JsonPrimitive(it) }

val jsonObjectFieldArb: Arb<Pair<String, JsonElement>> = arbitrary {
  val key = simpleStringArb.bind()
  val value = Arb.choice(jsonStringArb, jsonIntArb, jsonBooleanArb).bind()
  key to value
}

val functionObjectArb = arbitrary {
  val name = Arb.uuid().map { it.toString() }.bind()
  val fields = Arb.list(jsonObjectFieldArb).map { JsonObject(it.toMap()) }.bind()
  val description = Arb.string().orNull(0.5).bind()
  FunctionObject(name, fields, description)
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
