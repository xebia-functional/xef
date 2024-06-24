package com.xebia.functional.xef.llm

import com.xebia.functional.tokenizer.Encoding
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.prompt.completionRole
import com.xebia.functional.xef.prompt.contentAsString
import io.github.nomisrev.openapi.ChatCompletionRequestMessage

fun ModelType.tokensFromMessages(
  messages: List<ChatCompletionRequestMessage>,
  includePadding: Boolean = true
): Int { // TODO: naive implementation with magic numbers
  fun Encoding.countTokensFromMessages(tokensPerMessage: Int, tokensPerName: Int): Int =
    messages.sumOf { message ->
      countTokens(message.completionRole().value) +
        countTokens(message.contentAsString() ?: "") +
        tokensPerMessage +
        tokensPerName
    }
  return encoding.countTokensFromMessages(
    tokensPerMessage = tokensPerMessage,
    tokensPerName = tokensPerName
  ) + if (includePadding) tokenPadding + tokenPaddingSum else 0
}
