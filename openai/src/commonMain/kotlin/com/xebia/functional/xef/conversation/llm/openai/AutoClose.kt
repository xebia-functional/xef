package com.xebia.functional.xef.conversation.llm.openai

import com.aallam.openai.client.Closeable
import com.xebia.functional.xef.conversation.AutoClose

/** integration to aallam's [Closeable] */
internal fun <A : Closeable> AutoClose.autoClose(closeable: A): A {
  val wrapper =
    object : AutoCloseable {
      override fun close() = closeable.close()
    }
  autoClose(wrapper)
  return closeable
}
