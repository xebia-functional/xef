package com.xebia.functional.xef.conversation.futures

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.future.future

interface ExecutionContext : AutoCloseable {

  val executorService: ExecutorService
  val coroutineScope: CoroutineScope

  fun <A> future(block: suspend CoroutineScope.() -> A): CompletableFuture<A> =
    coroutineScope.future(coroutineScope.coroutineContext, CoroutineStart.DEFAULT, block)

  override fun close() {
    coroutineScope.cancel()
    executorService.shutdown()
  }

  private class AIScopeThreadFactory : ThreadFactory {
    private val counter = AtomicInteger()

    override fun newThread(r: Runnable): Thread {
      val t = Thread(r)
      t.name = "xef-ai-scope-worker-${counter.getAndIncrement()}"
      t.isDaemon = true
      return t
    }
  }

  companion object {
    @JvmField val DEFAULT = from(Executors.newCachedThreadPool(AIScopeThreadFactory()))

    @JvmStatic
    fun from(executorService: ExecutorService): ExecutionContext =
      object : ExecutionContext {
        override val executorService: ExecutorService = executorService
        override val coroutineScope: CoroutineScope =
          CoroutineScope(executorService.asCoroutineDispatcher())
      }
  }
}
