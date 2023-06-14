package com.xebia.functional.xef.auto.serialization.functions

import com.xebia.functional.xef.auto.serialization.buildJsonSchema
import com.xebia.functional.xef.llm.openai.functions.CFunction
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.*

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
fun encodeFunctionSchema(serialDescriptor: SerialDescriptor): List<CFunction> {
  return listOf(
    generateCFunction(serialDescriptor)
  )
}

private fun generateCFunction(descriptor: SerialDescriptor): CFunction {
  val parameters = buildJsonSchema(descriptor)
  val fnName = functionName(descriptor)
  return CFunction(fnName, "Generated function for $fnName", parameters)
}

@OptIn(ExperimentalSerializationApi::class)
internal fun functionName(descriptor: SerialDescriptor): String =
  descriptor.serialName.substringAfterLast(".")


@OptIn(ExperimentalSerializationApi::class)
private fun typeName(it: SerialDescriptor): String = when (it.kind) {
  PolymorphicKind.OPEN -> "object"
  PolymorphicKind.SEALED -> "object"
  PrimitiveKind.BOOLEAN -> "boolean"
  PrimitiveKind.BYTE -> "number"
  PrimitiveKind.CHAR -> "character"
  PrimitiveKind.DOUBLE -> "double"
  PrimitiveKind.FLOAT -> "float"
  PrimitiveKind.INT -> "number"
  PrimitiveKind.LONG -> "number"
  PrimitiveKind.SHORT -> "number"
  PrimitiveKind.STRING -> "string"
  SerialKind.CONTEXTUAL -> "object"
  SerialKind.ENUM -> "enum"
  StructureKind.CLASS -> "object"
  StructureKind.LIST -> "array"
  StructureKind.MAP -> "object"
  StructureKind.OBJECT -> "object"
}
