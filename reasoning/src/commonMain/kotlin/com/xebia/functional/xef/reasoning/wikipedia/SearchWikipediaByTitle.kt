package com.xebia.functional.xef.reasoning.wikipedia

import ai.xef.openai.OpenAIModel
import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import kotlin.jvm.JvmOverloads

class SearchWikipediaByTitle
@JvmOverloads
constructor(
  override val chatApi: ChatApi,
  override val model: OpenAIModel<CreateChatCompletionRequestModel>,
  override val scope: Conversation
) : SearchWikipediaByTitleTool {
  override val client: WikipediaClient = WikipediaClient()
}
