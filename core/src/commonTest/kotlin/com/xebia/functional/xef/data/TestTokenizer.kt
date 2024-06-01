package com.xebia.functional.xef.data

import ai.xef.Chat
import com.xebia.functional.xef.llm.ChatCompletionRequestMessage

class TestTokenizer : Chat.Tokenizer {
  override fun encode(text: String): List<Int> {
    TODO("Not yet implemented")
  }

  override fun truncateText(text: String, maxTokens: Int): String {
    TODO("Not yet implemented")
  }

  override fun tokensFromMessages(history: List<ChatCompletionRequestMessage>): Int {
    TODO("Not yet implemented")
  }
}
