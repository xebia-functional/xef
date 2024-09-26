package com.xebia.functional.xef.llm.assistants

import com.xebia.functional.openai.generated.model.FunctionObject
import com.xebia.functional.xef.llm.chatFunction
import com.xebia.functional.xef.llm.defaultFunctionDescription
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

fun interface Tool<Input, out Output> {
  suspend operator fun invoke(input: Input): Output

  data class JsonConfig(val inputJson: Json, val outputJson: Json) {
    companion object {
      val Default = JsonConfig(inputJson = Json.Default, outputJson = Json.Default)
    }
  }

  companion object {

    data class ToolConfig<Input, out Output>(
      val functionObject: FunctionObject,
      val serialization: ToolSerialization,
      val tool: Tool<Input, Output>
    )

    data class ToolSerialization(
      val inputSerializer: ToolSerializer,
      val outputSerializer: ToolSerializer
    )

    data class ToolSerializer(val serializer: KSerializer<*>, val json: Json)

    inline fun <reified I, reified O> toolOf(
      tool: Tool<I, O>,
      jsonConfig: JsonConfig = JsonConfig.Default
    ): ToolConfig<I, O> {
      val inputSerializer = ToolSerializer(serializer<I>(), jsonConfig.inputJson)
      val outputSerializer = ToolSerializer(serializer<O>(), jsonConfig.outputJson)
      val toolSerializer = ToolSerialization(inputSerializer, outputSerializer)
      val fn = chatFunction(inputSerializer.serializer.descriptor)
      val fnName = tool::class.simpleName ?: error("unnamed class")
      val fnDescription = defaultFunctionDescription(fnName)
      return ToolConfig(
        functionObject = fn.copy(name = fnName, description = fnDescription),
        serialization = toolSerializer,
        tool = tool
      )
    }
  }
}
