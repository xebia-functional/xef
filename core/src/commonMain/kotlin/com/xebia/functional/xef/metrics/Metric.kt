package com.xebia.functional.xef.metrics

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt

interface Metric {
  suspend fun <A> promptSpan(
    conversation: Conversation,
    prompt: Prompt,
    block: suspend Metric.() -> A
  ): A

  fun log(conversation: Conversation, message: String)

  companion object {
    val EMPTY: Metric =
      object : Metric {
        override suspend fun <A> promptSpan(
          conversation: Conversation,
          prompt: Prompt,
          block: suspend Metric.() -> A
        ): A = block()

        override fun log(conversation: Conversation, message: String) {}
      }
  }
}
