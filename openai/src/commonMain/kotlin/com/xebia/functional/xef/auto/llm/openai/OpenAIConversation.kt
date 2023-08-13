package com.xebia.functional.xef.auto.llm.openai

import com.xebia.functional.xef.auto.PlatformConversation
import kotlin.coroutines.cancellation.CancellationException

fun interface OpenAIConversation<A> {
  @Throws(CancellationException::class) fun conversation(conversation: PlatformConversation): A
}
