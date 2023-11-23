package com.xebia.functional.xef.llm.assistants

import com.xebia.functional.openai.infrastructure.ApiClient
import com.xebia.functional.openai.models.FunctionObject
import com.xebia.functional.xef.llm.chatFunction
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

fun interface Tool<out Output> {
  suspend operator fun invoke(): Output

  companion object {

    data class ToolSerializer(
      val inputSerializer: KSerializer<*>,
      val outputSerializer: KSerializer<*>
    )

    @PublishedApi internal val toolRegistry = mutableMapOf<String, ToolSerializer>()

    inline operator fun <reified T : Tool<O>, reified O> invoke(): FunctionObject {
      val serializer = serializer<T>()
      val outputSerializer = serializer<O>()
      val toolSerializer = ToolSerializer(serializer, outputSerializer)
      val fn = chatFunction(serializer.descriptor)
      if (toolRegistry.containsKey(fn.name)) {
        error("Function ${fn.name} already registered")
      }
      toolRegistry[fn.name] = toolSerializer
      return fn
    }

    suspend inline operator fun invoke(name: String, args: String): JsonElement {
      val toolSerializer = toolRegistry[name] ?: error("Function $name not registered")
      val input =
        ApiClient.JSON_DEFAULT.decodeFromString(toolSerializer.inputSerializer, args) as Tool<Any?>
      val output: Any? = input.invoke()
      return ApiClient.JSON_DEFAULT.encodeToJsonElement(
        toolSerializer.outputSerializer as KSerializer<Any?>,
        output
      )
    }
  }
}
