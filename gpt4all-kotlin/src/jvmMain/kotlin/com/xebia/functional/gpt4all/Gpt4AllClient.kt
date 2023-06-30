package com.xebia.functional.gpt4all

import com.xebia.functional.xef.llm.AIClient
import com.xebia.functional.xef.llm.models.chat.ChatCompletionRequest
import com.xebia.functional.xef.llm.models.chat.ChatCompletionResponse
import com.xebia.functional.xef.llm.models.chat.Message

class Gpt4AllClient : AIClient.Chat {
  override suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse {
    TODO("Not yet implemented")
  }

  override fun tokensFromMessages(messages: List<Message>): Int {
    TODO("Not yet implemented")
  }
}


