package com.xebia.functional.xef.auto.serialization.functions

import com.xebia.functional.xef.llm.openai.functions.CFunction
import com.xebia.functional.xef.llm.openai.functions.Parameters
import com.xebia.functional.xef.llm.openai.functions.Property
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

@OptIn(ExperimentalSerializationApi::class)
tailrec fun processDescriptor(
  descriptor: SerialDescriptor,
  properties: Map<String, Property> = mapOf(),
  required: List<String> = listOf(),
  index: Int = 0
): Parameters {
  // Base case: All elements have been processed
  if (index == descriptor.elementsCount) {
    return Parameters("object", properties, required)
  }

  // Process the current element
  val elementName = descriptor.getElementName(index)
  val elementDescriptor = descriptor.getElementDescriptor(index)
  val elementType = typeName(elementDescriptor)
  val newProperty = Property(elementType, "The $elementName") // Update description as needed
  val newProperties = properties + Pair(elementName, newProperty)
  val newRequired = if (!descriptor.isElementOptional(index)) required + elementName else required

  // Recursive case: Move to the next element
  return processDescriptor(descriptor, newProperties, newRequired, index + 1)
}

@OptIn(ExperimentalSerializationApi::class)
private fun generateCFunction(descriptor: SerialDescriptor): CFunction {
  val parameters = processDescriptor(descriptor)
  return CFunction(functionName(descriptor), "Generated function for ${descriptor.serialName}", parameters)
}

@OptIn(ExperimentalSerializationApi::class)
internal fun functionName(descriptor: SerialDescriptor): String =
  descriptor.serialName.substringAfterLast(".")


@OptIn(ExperimentalSerializationApi::class)
private fun typeName(it: SerialDescriptor): String = when (it.kind) {
  PolymorphicKind.OPEN -> "object"
  PolymorphicKind.SEALED -> "object"
  PrimitiveKind.BOOLEAN -> "boolean"
  PrimitiveKind.BYTE -> "byte"
  PrimitiveKind.CHAR -> "char"
  PrimitiveKind.DOUBLE -> "double"
  PrimitiveKind.FLOAT -> "float"
  PrimitiveKind.INT -> "int"
  PrimitiveKind.LONG -> "long"
  PrimitiveKind.SHORT -> "short"
  PrimitiveKind.STRING -> "string"
  SerialKind.CONTEXTUAL -> "object"
  SerialKind.ENUM -> "enum"
  StructureKind.CLASS -> "object"
  StructureKind.LIST -> "array"
  StructureKind.MAP -> "object"
  StructureKind.OBJECT -> "object"
}
