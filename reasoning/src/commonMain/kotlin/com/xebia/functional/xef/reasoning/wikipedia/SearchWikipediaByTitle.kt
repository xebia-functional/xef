package com.xebia.functional.xef.reasoning.wikipedia

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat
import kotlin.jvm.JvmOverloads

class SearchWikipediaByTitle @JvmOverloads constructor(override val model: Chat, override val scope: Conversation) :
  SearchWikipediaByTitleTool {
  override val client: WikipediaClient =
    WikipediaClient()
}
