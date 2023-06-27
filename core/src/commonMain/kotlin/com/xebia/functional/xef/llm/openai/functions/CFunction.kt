package com.xebia.functional.xef.llm.openai.functions

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonUnquotedLiteral

/*
"functions": [
    {
      "name": "get_current_weather",
      "description": "Get the current weather in a given location",
      "parameters": {
        "type": "object",
        "properties": {
          "location": {
            "type": "string",
            "description": "The city and state, e.g. San Francisco, CA"
          },
          "unit": {
            "type": "string",
            "enum": ["celsius", "fahrenheit"]
          }
        },
        "required": ["location"]
      }
    }
  ]
 */
@Serializable
data class CFunction(val name: String, val description: String, val parameters: RawJsonString)

typealias RawJsonString = @Serializable(with = RawJsonStringSerializer::class) String

@OptIn(ExperimentalSerializationApi::class)
private object RawJsonStringSerializer : KSerializer<String> {
  override val descriptor =
    PrimitiveSerialDescriptor(
      "com.xebia.functional.xef.llm.openai.functions.RawJsonString",
      PrimitiveKind.STRING
    )

  override fun deserialize(decoder: Decoder): String = decoder.decodeString()

  override fun serialize(encoder: Encoder, value: String) =
    when (encoder) {
      is JsonEncoder -> encoder.encodeJsonElement(JsonUnquotedLiteral(value))
      else -> encoder.encodeString(value)
    }
}
