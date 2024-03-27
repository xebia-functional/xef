package com.xebia.functional.xef.conversation.streaming

import com.xebia.functional.xef.AI
import kotlinx.coroutines.flow.Flow

suspend fun main() {
  AI<Flow<String>>("Create a 1000 word essay about Mars").collect { element -> print(element) }
}
