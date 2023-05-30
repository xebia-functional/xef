@file:OptIn(ExperimentalTime::class)

package com.xebia.functional.xef

import arrow.atomic.Atomic
import arrow.atomic.update
import arrow.atomic.value
import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.identity
import arrow.core.left
import arrow.core.nonFatalOrThrow
import arrow.core.raise.catch
import arrow.core.right
import arrow.resilience.Schedule
import arrow.resilience.ScheduleStep
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive


/**
 * A DSL that runs a _traceable_ [block],
 * it will track a [List] of individual [Trace]s recorded by [Tracer.trace].
 *
 * @return a [Pair] of all recorded [Trace]s and the final result of [A].
 */
suspend fun <A> tracer(
  timeSource: TimeSource = TimeSource.Monotonic,
  block: suspend Tracer.() -> A
): Pair<A, List<Trace>> {
  val history = Atomic<List<Trace>>(emptyList())
  val tracer = DefaultTracer(timeSource, timeSource.markNow(), history)
  return block(tracer) to history.value
}

interface Tracer {

  /**
   * Trace a [block].
   * any _NonFatal_ exceptions observed within [block] will be tracked,
   * such that if [trace] is surrounded with error-handling it will still appear in the trace.
   */
  suspend fun <A> trace(
    message: String? = null,
    show: suspend (value: A) -> String = { it.toString() },
    block: suspend () -> A
  ): A

  /**
   * Trace an [action] whilst retrying according to the provided [Schedule].
   * Tracks all exceptions that occurred when retrying.
   */
  suspend fun <A> Schedule<Throwable, *>.retry(
    message: String? = null,
    show: suspend (value: A) -> String = { it.toString() },
    action: suspend () -> A
  ): A

  /**
   * Creates a nested tracer that returns it's _local_ traces (starting from a new [TimeMark],
   * and appends its traces to the parent traces whilst respecting the parent's _start_.
   */
  suspend fun <A> tracer(block: suspend Tracer.() -> A): Pair<A, List<Trace>>

  fun appendTraces(traces: List<Trace>)

  fun appendTrace(trace: Trace) = appendTraces(listOf(trace))
}

data class Trace(
  val message: String?,
  val start: Duration,
  val finished: Duration,
  val result: Either<Throwable, String>
) {
  fun log(): String =
    "Trace(message=$message, duration=${finished - start} result=${
      result.fold(
        Throwable::stackTraceToString,
        ::identity
      )
    })"
}

fun List<Trace>.log(): String =
  withIndex().joinToString(separator = "\n---\n") { (_, trace) -> trace.log() }

private class DefaultTracer(
  private val timeSource: TimeSource,
  private val start: TimeMark,
  private val history: Atomic<List<Trace>>
) : Tracer {
  override suspend fun <A> trace(
    message: String?,
    show: suspend (value: A) -> String,
    block: suspend () -> A
  ): A {
    val traceStart = start.elapsedNow()
    return try {
      block()
        .also { a -> history.update { it + Trace(message, traceStart, start.elapsedNow(), show(a).right()) } }
    } catch (e: Throwable) {
      e.nonFatalOrThrow()
      history.update { it + Trace(message, traceStart, start.elapsedNow(), e.left()) }
      throw e
    }
  }


  override suspend fun <A> Schedule<Throwable, *>.retry(
    message: String?,
    show: suspend (value: A) -> String,
    action: suspend () -> A
  ): A {
    var step: ScheduleStep<Throwable, *> = step

    while (true) {
      currentCoroutineContext().ensureActive()
      val startRetry = start.elapsedNow()
      try {
        return action()
          .also { a -> history.update { it + Trace(message, startRetry, start.elapsedNow(), show(a).right()) } }
      } catch (e: Throwable) {
        when (val decision = step(e.nonFatalOrThrow())) {
          is Schedule.Decision.Continue -> {
            history.update { it + Trace(message, startRetry, start.elapsedNow(), e.left()) }
            if (decision.delay != Duration.ZERO) delay(decision.delay)
            step = decision.step
          }

          is Schedule.Decision.Done -> {
            history.update { it + Trace(message, startRetry, start.elapsedNow(), e.left()) }
            throw e
          }
        }
      }
    }
  }

  override suspend fun <A> tracer(block: suspend Tracer.() -> A): Pair<A, List<Trace>> {
    val parentDiff = start.elapsedNow()
    val nestedStart = timeSource.markNow()
    val history = Atomic<List<Trace>>(emptyList())
    val tracer = DefaultTracer(timeSource, nestedStart, history)
    return block(tracer) to history.value.also {
      appendTraces(history.value.map {
        it.copy(start = it.start + parentDiff, finished = it.finished + parentDiff)
      })
    }
  }

  override fun appendTraces(traces: List<Trace>) =
    history.update { it + traces }
}