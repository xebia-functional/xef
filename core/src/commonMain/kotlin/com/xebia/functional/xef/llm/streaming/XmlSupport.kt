package com.xebia.functional.xef.llm.streaming

import com.xebia.functional.openai.generated.model.ChatCompletionMessageToolCallFunction
import com.xebia.functional.openai.generated.model.ChatCompletionTool
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestStop
import com.xebia.functional.xef.Config
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*

object XmlSupport : FunctionCallFormat {
  override fun chatCompletionToolInstructions(tool: ChatCompletionTool): String =
    when (tool.type) {
      ChatCompletionTool.Type.function -> {
        val schema = tool.function.parameters
        val parameters = schema?.let(::jsonSchemaParametersToXml)
        val function = tool.function
        """
          <function>
            <name>${function.name}</name>
            <description>${function.description}</description>
            <parameters>${parameters}</parameters>
            <instructions>${toolsInvokeInstructions(tool)}</instructions>
          </function>
          """
          .trimIndent()
      }
      else -> ""
    }

  /** tool.function.parameters is a json schema and only the param names should be extracted */
  private fun toolsInvokeInstructions(tool: ChatCompletionTool): String =
    """
      <function_calls>
        <invoke>
          <tool_name>${tool.function.name}</tool_name>
          <parameters>
            ${tool.function.parameters?.let(::jsonSchemaParametersToXMLCallInstructions)}
          </parameters>
        </invoke>
      </function_calls>
      """
      .trimIndent()

  private fun jsonSchemaParametersToXMLCallInstructions(schema: JsonObject): String {
    val parameters = schema["properties"] as JsonObject
    return parameters.entries.joinToString(separator = "\n") { (key, _) ->
      "<$key>{{replace-with-value}}</$key>"
    }
  }

  private fun jsonSchemaParametersToXml(schema: JsonObject): String {
    val parameters = schema["properties"] as JsonObject
    return parameters.entries.joinToString("\n") { (key, value) ->
      """
        <parameter>
          <name>$key</name>
          <type>${value.jsonObject["type"]?.jsonPrimitive?.content}</type>
          <description>${value.jsonObject["description"]?.jsonPrimitive?.content}</description>
        </parameter>
        """
        .trimIndent()
    }
  }

  override fun createExampleFromSchema(schema: JsonElement): String {
    return schema.jsonObject["properties"]?.let { properties ->
      properties.jsonObject.entries.joinToString("\n") { (key, value) ->
        """
          <$key>${createExampleFromSchema(value)}</$key>
          """
          .trimIndent()
      }
    } ?: ""
  }

  override fun findPropertyPath(element: String, targetProperty: String): List<String>? {
    return emptyList()
  }

  override fun propertyValue(prop: String, currentArgs: String): JsonElement? {
    TODO("Not yet implemented")
  }

  override fun textProperty(propertyValue: JsonElement): String? {
    TODO("Not yet implemented")
  }

  override fun stopOn(): CreateChatCompletionRequestStop? =
    CreateChatCompletionRequestStop.CaseString("</function_calls>")

  override fun cleanArguments(functionCall: ChatCompletionMessageToolCallFunction): String {
    return "<invoke>" +
      functionCall.arguments.substringAfter("<invoke>").substringBeforeLast("</invoke") +
      "</invoke>"
  }

  /**
   * Here arguments is an XML string and we need to convert it to a JSON string
   *
   * arguments looks like :
   *
   * <invoke> <tool_name>Planet</tool_name> <parameters> <name>Mars</name>
   * <anotherProperty>value</anotherProperty> </parameters> </invoke>
   */
  override fun argumentsToJsonString(arguments: String): String {
    val converted = convertXmlToJson(arguments)
    return converted
  }

  // Function to parse XML and convert to JSON
  fun convertXmlToJson(xml: String): String {
    val parsedXml = parseXml(xml)
    return Config.DEFAULT.json.encodeToString(JsonObject.serializer(), parsedXml)
  }

  private val xmlElementRegex = "<(\\w+)>(.*?)</\\1>".toRegex()

  // Simple XML parser to convert XML string to a JSON Object
  fun parseXml(xml: String): JsonObject {
    val matches = xmlElementRegex.findAll(xml)
    val map = mutableMapOf<String, JsonElement>()

    for (match in matches) {
      val key = match.groupValues[1]
      val value = match.groupValues[2]
      if (xmlElementRegex.containsMatchIn(value)) {
        map[key] = parseXml(value)
      } else {
        map[key] = Config.DEFAULT.json.parseToJsonElement(value)
      }
    }
    return JsonObject(map)
  }
}
