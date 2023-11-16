package com.xebia.functional.xef.llm

import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestMessage
import com.xebia.functional.tokenizer.Encoding
import com.xebia.functional.tokenizer.ModelType

fun ModelType.tokensFromMessages(
  messages: List<ChatCompletionRequestMessage>
): Int { // TODO: naive implementation with magic numbers
  fun Encoding.countTokensFromMessages(tokensPerMessage: Int, tokensPerName: Int): Int =
    messages.sumOf { message ->
      countTokens(message.completionRole().name) +
        countTokens(message.contentAsString() ?: "") +
        tokensPerMessage +
        tokensPerName
    } + 3
  return encoding.countTokensFromMessages(
    tokensPerMessage = tokensPerMessage,
    tokensPerName = tokensPerName
  ) + tokenPadding
}
