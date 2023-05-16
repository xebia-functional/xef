package com.xebia.functional.loom

import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine

object TestFunctions {

  @JvmStatic
  fun <A> completed(a: A): suspend () -> A = {
    delay(1) // Do some suspension
    a
  }

  @JvmStatic
  fun <A> completed(): suspend (A) -> A = { a ->
    delay(1) // Do some suspension
    a
  }

  @JvmStatic
  fun <A> combine(combine: (A, A) -> A): suspend (A, A) -> A = { a, b ->
    delay(1) // Do some suspension
    combine(a, b)
  }

  @JvmStatic
  fun <A, E : Throwable> failure(e: E): suspend () -> A = {
    delay(1) // Do some suspension
    throw e
  }

  /**
   * A method that takes a [CompletableFuture] that will be completed with an [Throwable]. Which is
   * [CancellationException] in case of cancellation, or [Throwable] in case of error, and otherwise
   * `null`.
   */
  @JvmStatic
  fun forever(completableFuture: CompletableFuture<Throwable?>): suspend () -> Unit = {
    suspendCancellableCoroutine<Unit> { cont ->
      cont.invokeOnCancellation { t -> completableFuture.complete(t) }
    }
  }
}
