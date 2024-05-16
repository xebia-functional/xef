package com.xebia.functional.xef.llm.streaming

import com.xebia.functional.openai.generated.model.ChatCompletionMessageToolCallFunction
import com.xebia.functional.openai.generated.model.ChatCompletionTool
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestStop
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.llm.streaming.JsonSupport.PropertyType.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*

object JsonSupport : FunctionCallFormat {

  override fun chatCompletionToolInstructions(tool: ChatCompletionTool): String =
    when (tool.type) {
      ChatCompletionTool.Type.function -> {
        val schema = tool.function.parameters
        val parameters =
          schema?.let { Config.DEFAULT.json.encodeToString(JsonElement.serializer(), it) }
        val function = tool.function
        """
          <function>
            <name>${function.name}</name>
            <description>${function.description}</description>
            <parameters>${parameters}</parameters>
            <instructions>
              <info>
              - `response` is exclusively in JSON format, no other text should be present in the `response`.
              - The `response` follows the JSON schema defined in the parameters.
              - The `response` should be a valid JSON object.
              - The `response` follows the `example` structure provided in the instructions.
              </info>
              <example>
                ${toolsInvokeInstructions(tool)}
              </example>
            </instructions>
          </function>
          
          Reply in JSON now:
          """
          .trimIndent()
      }
      else -> ""
    }

  private fun toolsInvokeInstructions(tool: ChatCompletionTool): String =
    tool.function.parameters?.let(::createExampleFromSchema) ?: ""

  private val stringBody = """\"(.*?)\"""".toRegex()
  private val numberBody = "(-?\\d+(\\.\\d+)?)".toRegex()
  private val booleanBody = """(true|false)""".toRegex()
  private val arrayBody = """\[(.*?)\]""".toRegex()
  private val objectBody = """\{(.*?)\}""".toRegex()
  private val nullBody = """null""".toRegex()

  /**
   * The PropertyType enum represents the different types of properties that can be identified from
   * JSON. These include STRING, NUMBER, BOOLEAN, ARRAY, OBJECT, NULL, and UNKNOWN.
   *
   * STRING: Represents a property with a string value. NUMBER: Represents a property with a numeric
   * value. BOOLEAN: Represents a property with a boolean value. ARRAY: Represents a property that
   * is an array of values. OBJECT: Represents a property that is an object with key-value pairs.
   * NULL: Represents a property with a null value. UNKNOWN: Represents a property of unknown type.
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
   * Determines the type of property based on a partial chunk of it's body.
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

  override fun propertyValue(prop: String, currentArgs: String): JsonElement? {
    val remainingText = currentArgs.replace("\n", "")
    val body = remainingText.substringAfterLast("\"$prop\":").trim()
    // detect the type of the property
    val propertyType = propertyType(body)
    // extract the body of the property or if null don't report it
    val detectedBody = extractBody(propertyType, body) ?: return null
    // repack the body as a valid JSON string
    val propertyValueAsJson = repackBodyAsJsonString(propertyType, detectedBody)
    return propertyValueAsJson?.let { Config.DEFAULT.json.parseToJsonElement(it) }
  }

  /**
   * Searches for the content of the property within a given JsonElement.
   *
   * @param propertyValue The JsonElement to search within.
   * @return The text property as a String, or null if not found.
   */
  override fun textProperty(propertyValue: JsonElement): String? {
    return when (propertyValue) {
      // we don't report on properties holding objects since we report on the properties of the
      // object
      is JsonObject -> null
      is JsonArray -> propertyValue.map { textProperty(it) }.joinToString(", ")
      is JsonPrimitive -> propertyValue.content
      is JsonNull -> "null"
    }
  }

  override fun findPropertyPath(element: String, targetProperty: String): List<String>? {
    return findPropertyPathTailrec(
      listOf(Config.DEFAULT.json.parseToJsonElement(element) to emptyList()),
      targetProperty
    )
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
  override fun createExampleFromSchema(schema: JsonElement): String {
    val json =
      when {
        schema is JsonObject && schema.containsKey("type") -> {
          when (schema["type"]?.jsonPrimitive?.content) {
            "object" -> {
              val properties = schema["properties"] as? JsonObject
              val resultMap = mutableMapOf<String, JsonElement>()
              properties?.forEach { (key, value) ->
                resultMap[key] =
                  Config.DEFAULT.json.parseToJsonElement(createExampleFromSchema(value))
              }
              JsonObject(resultMap)
            }
            "array" -> {
              val items = schema["items"]
              val exampleItems =
                items?.let { Config.DEFAULT.json.parseToJsonElement(createExampleFromSchema(it)) }
              JsonArray(listOfNotNull(exampleItems))
            }
            "string" -> JsonPrimitive("{{string}}")
            "number" -> JsonPrimitive("{{number}}")
            "boolean" -> JsonPrimitive("{{boolean}}")
            "null" -> JsonPrimitive(null)
            else -> JsonPrimitive(null)
          }
        }
        else -> JsonPrimitive(null)
      }

    return Config.DEFAULT.json.encodeToString(JsonElement.serializer(), json)
  }

  override fun stopOn(): CreateChatCompletionRequestStop? = null

  override fun cleanArguments(functionCall: ChatCompletionMessageToolCallFunction): String {
    return "{" + functionCall.arguments.substringAfter("{").substringBeforeLast("}") + "}"
  }

  override fun argumentsToJsonString(arguments: String): String {
    return arguments
  }
}
