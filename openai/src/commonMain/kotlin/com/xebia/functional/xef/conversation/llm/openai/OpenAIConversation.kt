package com.xebia.functional.xef.conversation.llm.openai

import com.xebia.functional.xef.conversation.PlatformConversation
import kotlin.coroutines.cancellation.CancellationException

fun interface OpenAIConversation<A> {
  @Throws(CancellationException::class) fun conversation(conversation: PlatformConversation): A
}
