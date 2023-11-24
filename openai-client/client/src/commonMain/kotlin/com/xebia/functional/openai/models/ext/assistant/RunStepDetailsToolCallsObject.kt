package com.xebia.functional.openai.models.ext.assistant

import com.xebia.functional.openai.models.RunStepDetailsToolCallsObjectToolCallsInner
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RunStepDetailsToolCallsObject(

  /* An array of tool calls the run step was involved in. These can be associated with one of three types of tools: `code_interpreter`, `retrieval`, or `function`.  */
  @SerialName(value = "tool_calls")
  @Required
  val toolCalls: List<RunStepDetailsToolCallsObjectToolCallsInner>,

  /* Always `tool_calls`. */
  @SerialName(value = "type") @Required val type: Type = Type.tool_calls
) : RunStepObjectStepDetails {

  constructor(
    toolCalls: List<RunStepDetailsToolCallsObjectToolCallsInner>
  ) : this(toolCalls, Type.tool_calls)

  /**
   * Always `tool_calls`.
   *
   * Values: tool_calls
   */
  @Serializable
  enum class Type(val value: String) {
    @SerialName(value = "tool_calls") tool_calls("tool_calls")
  }
}
