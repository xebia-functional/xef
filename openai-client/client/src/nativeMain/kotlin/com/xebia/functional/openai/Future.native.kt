package com.xebia.functional.openai

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.Future as NativeConcurrentFuture

actual typealias CompletableFuture<T> = NativePromise<T>

class NativePromise<T>(value: NativeConcurrentFuture<T>) {
  private val value: NativeConcurrentFuture<T> = value

  fun get(): T = value.result

}

actual fun <T> CoroutineScope.future(block: suspend () -> T): CompletableFuture<T> {
  val worker = Worker.start()
  val future = worker.execute(TransferMode.SAFE, { block }, {
    runBlocking { it() }
  })
  return NativePromise(future)
}
