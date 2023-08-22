package com.xebia.functional.gpt4all

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.store.VectorStore
import com.xebia.functional.xef.tracing.Dispatcher
import com.xebia.functional.xef.tracing.createDispatcher

suspend inline fun <A> conversation(
  dispatcher: Dispatcher = createDispatcher(),
  store: VectorStore = LocalVectorStore(HuggingFaceLocalEmbeddings.DEFAULT),
  noinline block: suspend Conversation.() -> A
): A = block(Conversation(store, dispatcher))
