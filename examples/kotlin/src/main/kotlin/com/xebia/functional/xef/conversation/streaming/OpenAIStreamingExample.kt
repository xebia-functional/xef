package com.xebia.functional.xef.conversation.streaming

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.embeddings.EmbeddingsService
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.store.LocalVectorStore

suspend fun main() {
  val chat: Chat = OpenAI().DEFAULT_CHAT
  val embeddings = EmbeddingsService(OpenAI().DEFAULT_EMBEDDING)
  val scope = Conversation(LocalVectorStore(embeddings))
  chat.promptStreaming(prompt = Prompt("What is the meaning of life?"), scope = scope).collect {
    print(it)
  }
}
