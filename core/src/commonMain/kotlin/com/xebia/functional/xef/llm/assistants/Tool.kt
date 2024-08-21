package com.xebia.functional.xef.llm.assistants

import com.xebia.functional.openai.generated.model.FunctionObject
import com.xebia.functional.xef.llm.chatFunction
import com.xebia.functional.xef.llm.defaultFunctionDescription
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

fun interface Tool<Input, out Output> {
  suspend operator fun invoke(input: Input): Output

  companion object {

    data class ToolConfig<Input, out Output>(
      val functionObject: FunctionObject,
      val serializers: ToolSerializer,
      val tool: Tool<Input, Output>,
      val json: Json = Json.Default
    )

    data class ToolSerializer(
      val inputSerializer: KSerializer<*>,
      val outputSerializer: KSerializer<*>
    )

    inline fun <reified I, reified O> toolOf(tool: Tool<I, O>): ToolConfig<I, O> {
      val inputSerializer = serializer<I>()
      val outputSerializer = serializer<O>()
      val toolSerializer = ToolSerializer(inputSerializer, outputSerializer)
      val fn = chatFunction(inputSerializer.descriptor)
      val fnName = tool::class.simpleName ?: error("unnamed class")
      val fnDescription = defaultFunctionDescription(fnName)
      return ToolConfig(
        functionObject = fn.copy(name = fnName, description = fnDescription),
        serializers = toolSerializer,
        tool = tool
      )
    }
  }
}
