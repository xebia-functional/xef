package com.xebia.functional.xef.conversation

import arrow.atomic.Atomic
import arrow.atomic.update
import io.ktor.utils.io.core.*

/**
 * AutoClose offers DSL style API for creating parent-child relationships of AutoCloseable
 * dependencies
 */
interface AutoClose : AutoCloseable {
  fun <A : AutoCloseable> autoClose(autoCloseable: A): A
}

/** DSL method to use AutoClose */
fun <A> autoClose(block: AutoClose.() -> A): A = autoClose().use(block)

/** Constructor for AutoClose to be use for interface delegation of already scoped classes. */
fun autoClose(): AutoClose =
  object : AutoClose {
    private val finalizers: Atomic<List<() -> Unit>> = Atomic(emptyList())

    override fun <A : AutoCloseable> autoClose(autoCloseable: A): A {
      finalizers.update { prev -> prev + autoCloseable::close }
      return autoCloseable
    }

    override fun close() {
      finalizers
        .get()
        .fold<() -> Unit, Throwable?>(null) { acc, function ->
          acc.add(runCatching { function.invoke() }.exceptionOrNull())
        }
        ?.let { throw it }
    }
  }

/** integration to Ktor's [Closeable] */
fun <A : Closeable> AutoClose.autoClose(closeable: A): A {
  val wrapper =
    object : AutoCloseable {
      override fun close() = closeable.close()
    }
  autoClose(wrapper)
  return closeable
}

private fun Throwable?.add(other: Throwable?): Throwable? =
  this?.apply { other?.let { addSuppressed(it) } } ?: other
