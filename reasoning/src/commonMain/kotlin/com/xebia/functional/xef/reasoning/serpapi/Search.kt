package com.xebia.functional.xef.reasoning.serpapi

import ai.xef.openai.OpenAIModel
import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import kotlin.jvm.JvmOverloads

class Search
@JvmOverloads
constructor(
  private val serpApiKey: String,
  override val model: OpenAIModel<CreateChatCompletionRequestModel>,
  override val chatApi: ChatApi,
  override val scope: Conversation,
  override val maxResultsInContext: Int = 3
) : SearchTool {
  override val client: SerpApiClient = SerpApiClient(serpApiKey = serpApiKey)
}
