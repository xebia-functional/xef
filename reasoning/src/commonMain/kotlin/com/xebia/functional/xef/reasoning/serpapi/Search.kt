package com.xebia.functional.xef.reasoning.serpapi

import ai.xef.openai.OpenAIModel
import arrow.core.nonEmptyListOf
import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.llm.fromEnvironment
import kotlin.jvm.JvmOverloads

class Search
@JvmOverloads
constructor(
  private val serpApiKey: String =
    getenv("SERP_API_KEY") ?: throw AIError.Env.SerpApi(nonEmptyListOf("SERP_API_KEY not found")),
  override val model: OpenAIModel<CreateChatCompletionRequestModel>,
  override val chatApi: ChatApi = fromEnvironment(::ChatApi),
  override val scope: Conversation,
  override val maxResultsInContext: Int = 3
) : SearchTool {
  override val client: SerpApiClient = SerpApiClient(serpApiKey = serpApiKey)
}
