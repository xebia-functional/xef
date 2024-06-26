package com.xebia.functional.xef.llm.assistants

import com.xebia.functional.xef.llm.chatFunction
import com.xebia.functional.xef.openapi.FunctionObject
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

fun interface Tool<Input, out Output> {
  suspend operator fun invoke(input: Input): Output

  companion object {

    data class ToolConfig<Input, out Output>(
      val functionObject: FunctionObject,
      val serializers: ToolSerializer,
      val tool: Tool<Input, Output>
    )

    data class ToolSerializer(
      val inputSerializer: KSerializer<*>,
      val outputSerializer: KSerializer<*>
    )

    inline fun <reified I, reified O> toolOf(tool: Tool<I, O>): ToolConfig<I, O> {
      val serializer = serializer<I>()
      val outputSerializer = serializer<O>()
      val toolSerializer = ToolSerializer(serializer, outputSerializer)
      val fn = chatFunction(serializer.descriptor)
      return ToolConfig(
        fn.copy(name = tool::class.simpleName ?: error("unnamed class")),
        toolSerializer,
        tool
      )
    }
  }
}
