package com.xebia.functional.xef.llm.assistants

import com.xebia.functional.openai.generated.model.CreateAssistantRequestModel
import com.xebia.functional.openai.generated.model.CreateAssistantRequestToolResources
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssistantRequest(
  @SerialName(value = "assistant_id") val assistantId: String? = null,
  @SerialName(value = "model") @Required val model: CreateAssistantRequestModel,

  /** The name of the assistant. The maximum length is 256 characters. */
  @SerialName(value = "name") val name: String? = null,

  /** The description of the assistant. The maximum length is 512 characters. */
  @SerialName(value = "description") val description: String? = null,

  /** The system instructions that the assistant uses. The maximum length is 32768 characters. */
  @SerialName(value = "instructions") val instructions: String? = null,

  /**
   * A list of tool enabled on the assistant. There can be a maximum of 128 tools per assistant.
   * Tools can be of types `code_interpreter`, `retrieval`, or `function`.
   */
  @SerialName(value = "tools") val tools: List<AssistantTool>? = arrayListOf(),
  @SerialName(value = "tool_resources")
  val toolResources: CreateAssistantRequestToolResources? = null,

  /**
   * Set of 16 key-value pairs that can be attached to an object. This can be useful for storing
   * additional information about the object in a structured format. Keys can be a maximum of 64
   * characters long and values can be a maxium of 512 characters long.
   */
  @SerialName(value = "metadata") val metadata: Map<String, String>? = null,

  /**
   * What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output
   * more random, while lower values like 0.2 will make it more focused and deterministic.
   */
  @SerialName(value = "temperature") val temperature: Double? = null,

  /**
   * An alternative to sampling with temperature, called nucleus sampling, where the model considers
   * the results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising
   * the top 10% probability mass are considered.
   */
  @SerialName(value = "top_p") val topP: Double? = null,
)

@Serializable
sealed class AssistantTool {
  @Serializable @SerialName(value = "code_interpreter") object CodeInterpreter : AssistantTool()

  @Serializable @SerialName(value = "retrieval") object Retrieval : AssistantTool()

  @Serializable
  @SerialName(value = "function")
  data class Function(val name: String, val description: String, val parameters: String) :
    AssistantTool()
}
