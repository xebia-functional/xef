@file:JvmName("LoomRunner")

package com.xebia.functional.loom

import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking

/**
 * A Loom based [runBlocking], this method will call [runBlocking] using a Loom based dispatcher.
 *
 * **Note**: This method **should only be used from another VirtualThread**, if called from a
 * platform thread it will still block the current thread.
 */
@Throws(InterruptedException::class)
@JvmName("run")
fun <A> runLoom(block: suspend CoroutineScope.(dispatcher: CoroutineDispatcher) -> A): A =
  Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher().use { loom ->
    runBlocking(context = loom) { block(loom) }
  }
