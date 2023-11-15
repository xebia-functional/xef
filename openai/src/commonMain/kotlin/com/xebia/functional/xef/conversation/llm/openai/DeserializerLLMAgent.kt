package com.xebia.functional.xef.conversation.llm.openai

import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.Prompt
import kotlinx.serialization.SerializationException
import kotlinx.serialization.serializer

/**
 * Run a [prompt] describes the task you want to solve within the context of [AIScope]. Returns a
 * value of [A] where [A] **has to be** annotated with [kotlinx.serialization.Serializable].
 *
 * @throws SerializationException if serializer cannot be created (provided [A] or its type argument
 *   is not serializable).
 * @throws IllegalArgumentException if any of [A]'s type arguments contains star projection.
 */
@AiDsl
suspend inline fun <reified A> Conversation.prompt(model: ChatWithFunctions, prompt: Prompt): A =
  model.prompt(prompt, serializer())
