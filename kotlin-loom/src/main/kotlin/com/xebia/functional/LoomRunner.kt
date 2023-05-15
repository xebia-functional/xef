@file:JvmName("LoomRunner")

package com.xebia.functional

import java.util.concurrent.Executors
import java.util.concurrent.locks.LockSupport
import kotlin.jvm.Throws
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking

/**
 * A Loom based [runBlocking], this method will call `runBlocking` using a Loom based dispatcher.
 * This method should only be used from a VirtualThread, or it will still block the current thread.
 *
 * Underneath [runBlocking] uses [LockSupport.parkNanos] supports Loom.
 */
@Throws(InterruptedException::class)
@JvmName("run")
fun <A> runLoom(block: suspend CoroutineScope.() -> A): A =
  Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher().use { loom ->
    runBlocking(context = loom) { block() }
  }
