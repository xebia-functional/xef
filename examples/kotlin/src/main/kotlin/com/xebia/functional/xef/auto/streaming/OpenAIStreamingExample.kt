package com.xebia.functional.xef.auto.streaming

import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.OpenAIEmbeddings
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.vectorstores.LocalVectorStore

suspend fun main() {
  val chat: Chat = OpenAI.DEFAULT_CHAT
  val embeddings = OpenAIEmbeddings(OpenAI.DEFAULT_EMBEDDING)
  val vectorStore = LocalVectorStore(embeddings)
  // hack until https://github.com/nomic-ai/gpt4all/pull/1126 is accepted or merged
  val out = System.out
  chat.promptStreaming(
    question = "What is the meaning of life?",
    context = vectorStore
  ).collect {
    out.print(it)
  }
}

