package com.xebia.functional.xef.aws.bedrock

import com.xebia.functional.openai.generated.model.ChatCompletionRequestMessage
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class AnthropicMessagesRequestBody(
  @Required val messages: List<ChatCompletionRequestMessage>,
  @Required @SerialName("anthropic_version") val anthropicVersion: String = "bedrock-2023-05-31",
  @Required @SerialName("max_tokens") val maxTokens: Int = 3000,
  val system: String? = null,
  @Required val temperature: Double? = null,
  @Required @SerialName("top_p") val topP: Double? = null,
  @Required @SerialName("top_k") val topK: Int? = null,
  @Required @SerialName("stop_sequences") val stopSequences: List<String>? = null,

  /**
   * This is an issue also reported in https://github.com/langchain-ai/langchain/issues/20320 As of
   * now, the `tools` field is not documented in the Bedrock API documentation. Users are advised to
   * use https://docs.anthropic.com/claude/docs/legacy-tool-use which we will have to implement if
   * we use bedrock instead of anthropic's api directly.
   *
   * Note: The new tool use format is not yet available on Vertex AI or Amazon Bedrock, but is
   * coming soon to those platforms.
   */
  val tools: List<AnthropicChatCompletionTool>? = null
)

/*
{
        "name": "get_weather",
        "description": "Get the current weather in a given location",
        "input_schema": {
          "type": "object",
          "properties": {
            "location": {
              "type": "string",
              "description": "The city and state, e.g. San Francisco, CA"
            }
          },
          "required": ["location"]
        }
      }
 */

@Serializable
data class AnthropicChatCompletionTool(
  @Required val name: String,
  @Required val description: String,
  @Required @SerialName("input_schema") val inputSchema: JsonElement
)
