package com.xebia.functional.gpt4all

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.store.VectorStore

suspend inline fun <A> conversation(
  store: VectorStore = LocalVectorStore(HuggingFaceLocalEmbeddings.DEFAULT),
  noinline block: suspend Conversation.() -> A
): A = block(Conversation(store, provider = TODO("provider for gpt4all")))
