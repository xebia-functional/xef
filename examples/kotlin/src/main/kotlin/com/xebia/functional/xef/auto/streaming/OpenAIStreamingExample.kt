package com.xebia.functional.xef.auto.streaming

import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.OpenAIEmbeddings
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.vectorstores.LocalVectorStore

suspend fun main() {
  val chat: Chat = OpenAI.DEFAULT_CHAT
  val embeddings = OpenAIEmbeddings(OpenAI.DEFAULT_EMBEDDING)
  val vectorStore = LocalVectorStore(embeddings)
  chat.promptStreaming(
    question = "What is the meaning of life?",
    context = vectorStore
  ).collect {
    print(it)
  }
}

