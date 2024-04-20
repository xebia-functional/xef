package com.xebia.functional.openai

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.intrinsics.startCoroutineCancellable
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(InternalCoroutinesApi::class)
actual fun <T> CoroutineScope.future(block: suspend () -> T): CompletableFuture<T> {
  return JsPromise { resolve, reject ->
    block.startCoroutineCancellable(object : Continuation<T> {
      override val context: CoroutineContext = EmptyCoroutineContext

      override fun resumeWith(result: Result<T>) {
        result.onSuccess { resolve(it) }
        result.onFailure { reject(it) }
      }
    })
  }
}
