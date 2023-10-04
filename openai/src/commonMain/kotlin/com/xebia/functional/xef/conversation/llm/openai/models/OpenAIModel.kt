package com.xebia.functional.xef.conversation.llm.openai.models

import com.xebia.functional.tokenizer.Encoding
import com.xebia.functional.tokenizer.EncodingType
import com.xebia.functional.tokenizer.truncateText
import com.xebia.functional.xef.llm.LLM
import com.xebia.functional.xef.llm.models.chat.Message

interface OpenAIModel : LLM {

  val encodingType: EncodingType
  val encoding
    get() = encodingType.encoding

  override fun tokensFromMessages(messages: List<Message>): Int { // intermediary solution
    fun Encoding.countTokensFromMessages(tokensPerMessage: Int, tokensPerName: Int): Int =
      messages.sumOf { message ->
        countTokens(message.role.name) +
          countTokens(message.content) +
          tokensPerMessage +
          tokensPerName
      } + 3
    return encoding.countTokensFromMessages(tokensPerMessage = 5, tokensPerName = 5) + 10
  }

  override fun truncateText(text: String, maxTokens: Int): String =
    encoding.truncateText(text, maxTokens)
}
