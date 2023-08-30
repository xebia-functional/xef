package com.xebia.functional.xef.reasoning.wikipedia

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat
import kotlin.jvm.JvmOverloads

expect class SearchWikipedia
@JvmOverloads
constructor(model: Chat, scope: Conversation, maxResultsInContext: Int = 3) : SearchWikipediaTool
