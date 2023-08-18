package com.xebia.functional.gpt4all

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.vectorstores.LocalVectorStore
import com.xebia.functional.xef.vectorstores.VectorStore

suspend inline fun <A> conversation(
  store: VectorStore = LocalVectorStore(HuggingFaceLocalEmbeddings.DEFAULT),
  noinline block: suspend Conversation.() -> A
): A = block(Conversation(store))
