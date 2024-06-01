package com.xebia.functional.xef

import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt

interface AI<A: Any> {

  @AiDsl
  suspend operator fun invoke(prompt: Prompt, conversation: Conversation = Conversation()): A

  @AiDsl
  suspend operator fun invoke(prompt: String, conversation: Conversation = Conversation()): A =
    invoke(Prompt(prompt), conversation)
}
