package com.xebia.functional.xef.reasoning.wikipedia

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat

actual class SearchWikipediaByTitle
actual constructor(override val model: Chat, override val scope: Conversation) :
  SearchWikipediaByTitleTool, AutoCloseable {
  override val client: WikipediaClient = WikipediaClient()

  override fun close() {
    client.close()
  }
}
