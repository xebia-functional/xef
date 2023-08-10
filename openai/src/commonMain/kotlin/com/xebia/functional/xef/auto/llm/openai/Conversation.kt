package com.xebia.functional.xef.auto.llm.openai

import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.vectorstores.LocalVectorStore
import com.xebia.functional.xef.vectorstores.VectorStore

suspend inline fun <A> conversation(
  store: VectorStore = LocalVectorStore(OpenAIEmbeddings(OpenAI.DEFAULT_EMBEDDING)),
  noinline block: suspend Conversation.() -> A
): A = block(Conversation(store))
