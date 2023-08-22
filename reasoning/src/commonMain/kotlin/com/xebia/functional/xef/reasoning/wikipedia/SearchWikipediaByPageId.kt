package com.xebia.functional.xef.reasoning.wikipedia

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat
import kotlin.jvm.JvmOverloads

expect class SearchWikipediaByPageId @JvmOverloads constructor(model: Chat, scope: Conversation) :
  SearchWikipediaByPageIdTool
