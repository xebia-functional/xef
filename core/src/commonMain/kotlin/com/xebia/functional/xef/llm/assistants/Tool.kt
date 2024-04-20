package com.xebia.functional.xef.llm.assistants

import com.xebia.functional.openai.generated.model.FunctionObject
import com.xebia.functional.xef.llm.chatFunction
import com.xebia.functional.xef.serialization.Serializer

fun interface Tool<Input, out Output> {
  suspend operator fun invoke(input: Input): Output

  companion object {

    data class ToolConfig<Input, out Output>(
      val functionObject: FunctionObject,
      val serializers: ToolSerializer,
      val tool: Tool<Input, Output>
    )

    data class ToolSerializer(
      val inputSerializer: Serializer<*>,
      val outputSerializer: Serializer<*>
    )

    inline fun <reified I: Any, reified O: Any> toolOf(tool: Tool<I, O>): ToolConfig<I, O> {
      val serializer = Serializer<I>()
      val outputSerializer = Serializer<O>()
      val toolSerializer = ToolSerializer(serializer, outputSerializer)
      val fn = chatFunction(serializer)
      return ToolConfig(
        fn.copy(name = tool::class.simpleName ?: error("unnamed class")),
        toolSerializer,
        tool
      )
    }
  }
}
