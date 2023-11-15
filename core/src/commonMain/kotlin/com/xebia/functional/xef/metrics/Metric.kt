package com.xebia.functional.xef.metrics

import com.xebia.functional.xef.prompt.Prompt

interface Metric {
  suspend fun <A> customSpan(name: String, block: suspend Metric.() -> A): A

  suspend fun <A, T> promptSpan(prompt: Prompt<T>, block: suspend Metric.() -> A): A

  fun event(message: String)

  fun parameter(key: String, value: String)

  companion object {
    val EMPTY: Metric =
      object : Metric {
        override suspend fun <A> customSpan(name: String, block: suspend Metric.() -> A): A =
          block()

        override suspend fun <A, T> promptSpan(prompt: Prompt<T>, block: suspend Metric.() -> A): A =
          block()

        override fun event(message: String) {}

        override fun parameter(key: String, value: String) {}
      }
  }
}
