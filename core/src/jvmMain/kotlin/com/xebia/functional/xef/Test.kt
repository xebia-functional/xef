package com.xebia.functional.xef

import arrow.core.NonEmptyList
import arrow.core.identity
import arrow.fx.coroutines.parZip
import arrow.resilience.Schedule
import com.xebia.functional.xef.llm.openai.History
import com.xebia.functional.xef.llm.openai.retryTimed
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.delay

suspend fun main() {
  var counter = 0
  Schedule.recurs<Throwable>(2)
    .retryTimed {
      parZip(
        { delay(1000) },
        {
          delay(100)
          if (counter++ < 2) throw IOException()
        }
      ) { _, _ -> "Finished" }
    }.let { println(it.log()) }
}

fun <A> NonEmptyList<History<A>>.log(): String =
  withIndex().joinToString(separator = "\n---\n") { (index, history) ->
    "Attempt ${size - index} took ${history.duration} and ${
      history.result.fold(
        { "failed" },
        { "succeeded" })
    } with: ${history.result.fold({ it.stackTraceToString() }, ::identity)}"
  }
