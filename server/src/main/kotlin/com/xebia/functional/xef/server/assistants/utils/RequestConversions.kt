package com.xebia.functional.xef.server.assistants.utils

import com.xebia.functional.openai.generated.model.*

object RequestConversions {
  fun CreateThreadAndRunRequestToolsInner.assistantObjectToolsInner(): AssistantObjectToolsInner =
    when (this) {
      is CreateThreadAndRunRequestToolsInner.CaseAssistantToolsCode ->
        AssistantObjectToolsInner.CaseAssistantToolsCode(AssistantToolsCode(AssistantToolsCode.Type.code_interpreter))

      is CreateThreadAndRunRequestToolsInner.CaseAssistantToolsFunction ->
        AssistantObjectToolsInner.CaseAssistantToolsFunction(
          AssistantToolsFunction(
            type = AssistantToolsFunction.Type.function,
            function = value.function
          )
        )

      is CreateThreadAndRunRequestToolsInner.CaseAssistantToolsRetrieval ->
        AssistantObjectToolsInner.CaseAssistantToolsRetrieval(AssistantToolsRetrieval(AssistantToolsRetrieval.Type.retrieval))
    }
}
