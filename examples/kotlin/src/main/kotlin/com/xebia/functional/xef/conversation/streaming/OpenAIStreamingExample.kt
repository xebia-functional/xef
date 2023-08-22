package com.xebia.functional.xef.conversation.streaming

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.log
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.tracing.createDispatcher

suspend fun main() {
  val dispatcher = createDispatcher(OpenAI.log)
  val embeddings = OpenAI(dispatcher = dispatcher).DEFAULT_EMBEDDING

  OpenAI.conversation(store = LocalVectorStore(embeddings), dispatcher = dispatcher) {
    val chat: Chat = OpenAI().DEFAULT_CHAT

    chat.promptStreaming(prompt = Prompt("What is the meaning of life?"), scope = this).collect {
      print(it)
    }
  }
}
