package com.xebia.functional.xef.reasoning.wikipedia

import ai.xef.openai.OpenAIModel
import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.fromEnvironment
import kotlin.jvm.JvmOverloads

class SearchWikipediaByPageId
@JvmOverloads
constructor(
  override val model: OpenAIModel<CreateChatCompletionRequestModel>,
  override val scope: Conversation,
  override val chatApi: ChatApi = fromEnvironment(::ChatApi),
) : SearchWikipediaByPageIdTool {
  override val client: WikipediaClient = WikipediaClient()
}
