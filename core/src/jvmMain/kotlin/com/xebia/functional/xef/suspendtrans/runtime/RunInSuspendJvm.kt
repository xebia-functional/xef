@file:JvmName("RunInSuspendJvmKt")
package com.xebia.functional.xef.suspendtrans.runtime


import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext


// private val

@Suppress("ObjectPropertyName", "unused")
private val `$CoroutineContext4J$`: CoroutineContext = Dispatchers.IO

@Suppress("ObjectPropertyName", "unused")
private val `$CoroutineScope4J$`: CoroutineScope = CoroutineScope(`$CoroutineContext4J$`)

@Suppress("FunctionName")
@Deprecated("Just for generate.", level = DeprecationLevel.HIDDEN)
@Throws(InterruptedException::class)
public fun <T> `$runInBlocking$`(block: suspend () -> T): T = runBlocking(`$CoroutineContext4J$`) { block() }


private val classLoader: ClassLoader
  get() = Thread.currentThread().contextClassLoader ?: ClassLoader.getSystemClassLoader()

private val jdk8Support: Boolean
  get() = runCatching {
    classLoader.loadClass("kotlinx.coroutines.future.FutureKt")
    true
  }.getOrElse { false }

private interface FutureTransformer {
  fun <T> trans(scope: CoroutineScope, block: suspend () -> T): CompletableFuture<T>
}

private object CoroutinesJdk8Transformer : FutureTransformer {
  override fun <T> trans(scope: CoroutineScope, block: suspend () -> T): CompletableFuture<T> {
    return scope.future { block() }
  }
}

private object SimpleTransformer : FutureTransformer {
  @OptIn(ExperimentalCoroutinesApi::class)
  override fun <T> trans(scope: CoroutineScope, block: suspend () -> T): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    val deferred = scope.async { block() }
    future.whenComplete { _, exception ->
      deferred.cancel(exception?.let {
        it as? CancellationException ?: CancellationException(
          "CompletableFuture was completed exceptionally",
          it
        )
      })
    }
    deferred.invokeOnCompletion {
      try {
        future.complete(deferred.getCompleted())
      } catch (t: Throwable) {
        future.completeExceptionally(t)
      }
    }
    return future
  }
}

private val transformer: FutureTransformer =
  if (jdk8Support) CoroutinesJdk8Transformer
  else SimpleTransformer


@Deprecated("Just for generate.", level = DeprecationLevel.HIDDEN)
@Suppress("FunctionName")
public fun <T> `$runInAsync$`(
  block: suspend () -> T,
  scope: CoroutineScope = `$CoroutineScope4J$`
): CompletableFuture<T> {
  return transformer.trans(scope, block)
}
