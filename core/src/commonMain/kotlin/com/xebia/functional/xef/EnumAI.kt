package com.xebia.functional.xef

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt

interface EnumAI<E>: AI<E> where E : Enum<E> {

  val enum: E

  fun enumValueOf(value: String): E

  suspend fun invokeEnum(
    prompt: Prompt,
    conversation: Conversation
  ): E =
    invoke(
      prompt = prompt,
      conversation = conversation
    )

}
