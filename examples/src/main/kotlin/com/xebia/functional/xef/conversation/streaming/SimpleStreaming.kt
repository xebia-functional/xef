package com.xebia.functional.xef.conversation.streaming

import ai.xef.OpenAI
import kotlinx.coroutines.flow.Flow

suspend fun main() {
  val ai = OpenAI.Chat.gpt4o()
  ai<Flow<String>>("Create a 1000 word essay about Mars").collect { element -> print(element) }
}
