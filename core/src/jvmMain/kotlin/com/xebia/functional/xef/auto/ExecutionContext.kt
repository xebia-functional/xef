package com.xebia.functional.xef.auto

import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class ExecutionContext @JvmOverloads constructor(
  private val executorService: ExecutorService = Executors.newCachedThreadPool(
    AIScopeThreadFactory()
  ),
) : AutoCloseable {

  private val coroutineScope: CoroutineScope = CoroutineScope(
    executorService.asCoroutineDispatcher().plus(Job(null))
  )

  fun <A> future(block: suspend () -> A): CompletableFuture<A> {
    return coroutineScope.future(
      coroutineScope.coroutineContext, CoroutineStart.DEFAULT
    ) { block.invoke() }
  }

  override fun close() {
    coroutineScope.cancel()
    executorService.shutdown()
  }

  private class AIScopeThreadFactory : ThreadFactory {
    private val counter = AtomicInteger()
    override fun newThread(r: Runnable): Thread {
      val t = Thread(r)
      t.setName("xef-ai-scope-worker-" + counter.getAndIncrement())
      t.setDaemon(true)
      return t
    }
  }
}
