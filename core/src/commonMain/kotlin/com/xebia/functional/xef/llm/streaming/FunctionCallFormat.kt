package com.xebia.functional.xef.llm.streaming

import com.xebia.functional.openai.generated.model.ChatCompletionMessageToolCallFunction
import com.xebia.functional.openai.generated.model.ChatCompletionTool
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestStop
import kotlinx.serialization.json.JsonElement

sealed interface FunctionCallFormat {
  fun createExampleFromSchema(schema: JsonElement): String

  fun findPropertyPath(element: String, targetProperty: String): List<String>?

  fun chatCompletionToolInstructions(tool: ChatCompletionTool): String

  fun propertyValue(prop: String, currentArgs: String): JsonElement?

  fun textProperty(propertyValue: JsonElement): String?

  fun stopOn(): CreateChatCompletionRequestStop?

  fun cleanArguments(functionCall: ChatCompletionMessageToolCallFunction): String

  fun argumentsToJsonString(arguments: String): String
}
