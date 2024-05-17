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

  /* The name of the assistant. The maximum length is 256 characters.  */
  @SerialName(value = "name") val name: String? = null,

  /* The description of the assistant. The maximum length is 512 characters.  */
  @SerialName(value = "description") val description: String? = null,

  /* The system instructions that the assistant uses. The maximum length is 32768 characters.  */
  @SerialName(value = "instructions") val instructions: String? = null,

  /* A list of tool enabled on the assistant. There can be a maximum of 128 tools per assistant. Tools can be of types `code_interpreter`, `retrieval`, or `function`.  */
  @SerialName(value = "tools") val tools: List<AssistantTool>? = arrayListOf(),
  @SerialName(value = "tool_resources")
  val toolResources: CreateAssistantRequestToolResources? = null,

  /* Set of 16 key-value pairs that can be attached to an object. This can be useful for storing additional information about the object in a structured format. Keys can be a maximum of 64 characters long and values can be a maxium of 512 characters long.  */
  @SerialName(value = "metadata") val metadata: Map<String, String>? = null
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
