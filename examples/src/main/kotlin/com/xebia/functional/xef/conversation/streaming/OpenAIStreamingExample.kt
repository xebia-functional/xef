package com.xebia.functional.xef.conversation.streaming

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.metrics.LogsMetric
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.store.LocalVectorStore

suspend fun main() {
  val chat: Chat = OpenAI.fromEnvironment().DEFAULT_CHAT
  val embeddings = OpenAI.fromEnvironment().DEFAULT_EMBEDDING
  val scope = Conversation(LocalVectorStore(embeddings), LogsMetric())
  chat.promptStreaming(prompt = Prompt("What is the meaning of life?"), scope = scope).collect {
    print(it)
  }
}
