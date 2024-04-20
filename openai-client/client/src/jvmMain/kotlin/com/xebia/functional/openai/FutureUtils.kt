package com.xebia.functional.openai

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future

actual fun <T> CoroutineScope.future(block: suspend () -> T): CompletableFuture<T> =
  future {
    block()
  }
