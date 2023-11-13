package com.xebia.functional.xef.reasoning.serpapi

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat
import kotlin.jvm.JvmOverloads

class Search
@JvmOverloads
constructor(
  private val serpApiKey: String,
  override val model: Chat, override val scope: Conversation, override val maxResultsInContext: Int = 3
) : SearchTool {
  override val client: SerpApiClient =
    SerpApiClient(serpApiKey = serpApiKey)
}
