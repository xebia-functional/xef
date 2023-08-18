package com.xebia.functional.xef.reasoning.serpapi

import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.llm.Chat
import kotlin.jvm.JvmOverloads

expect class Search
@JvmOverloads
constructor(model: Chat, scope: Conversation, maxResultsInContext: Int = 3) : SearchTool
