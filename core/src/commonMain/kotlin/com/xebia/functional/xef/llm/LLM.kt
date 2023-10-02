package com.xebia.functional.xef.llm

import com.xebia.functional.tokenizer.Encoding
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.models.chat.Message

sealed interface LLM : AutoCloseable {

  val modelType: ModelType

  @Deprecated("use modelType.name instead", replaceWith = ReplaceWith("modelType.name"))
  val name
    get() = modelType.name

  /**
   * Copies this instance and uses [modelType] for [LLM.modelType].
   * Has to return the most specific type of this instance!
   */
  fun copy(modelType: ModelType): LLM

  fun tokensFromMessages(
    messages: List<Message>
  ): Int { // TODO: naive implementation with magic numbers
    fun Encoding.countTokensFromMessages(tokensPerMessage: Int, tokensPerName: Int): Int =
      messages.sumOf { message ->
        countTokens(message.role.name) +
          countTokens(message.content) +
          tokensPerMessage +
          tokensPerName
      } + 3
    return modelType.encoding.countTokensFromMessages(
      tokensPerMessage = modelType.tokensPerMessage,
      tokensPerName = modelType.tokensPerName
    ) + modelType.tokenPadding
  }

  override fun close() = Unit
}
