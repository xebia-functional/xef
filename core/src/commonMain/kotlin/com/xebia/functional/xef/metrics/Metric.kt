package com.xebia.functional.xef.metrics

import com.xebia.functional.openai.models.RunObject
import com.xebia.functional.openai.models.RunStepObject
import com.xebia.functional.xef.prompt.Prompt

interface Metric {
  suspend fun <A> customSpan(name: String, block: suspend Metric.() -> A): A

  suspend fun <A, T> promptSpan(prompt: Prompt<T>, block: suspend Metric.() -> A): A

  suspend fun event(message: String)

  suspend fun parameter(key: String, value: String)

  suspend fun parameter(key: String, values: List<String>)

  suspend fun assistantCreateRun(runObject: RunObject)

  suspend fun assistantCreateRun(runId: String, block: Metric.() -> RunObject): RunObject

  suspend fun assistantCreateRunStep(runId: String, block: Metric.() -> RunStepObject): RunStepObject

  suspend fun assistantToolOutputsRun(runId: String, block: suspend Metric.() -> RunObject): RunObject

  companion object {
    val EMPTY: Metric =
      object : Metric {
        override suspend fun <A> customSpan(name: String, block: suspend Metric.() -> A): A =
          block()

        override suspend fun <A, T> promptSpan(
          prompt: Prompt<T>,
          block: suspend Metric.() -> A
        ): A = block()

        override suspend fun assistantCreateRun(runObject: RunObject) {}

        override suspend fun assistantCreateRun(
          runId: String,
          block: Metric.() -> RunObject
        ): RunObject = block()

        override suspend fun assistantCreateRunStep(runId: String, block: Metric.() -> RunStepObject): RunStepObject =
          block()

        override suspend fun assistantToolOutputsRun(runId: String, block: suspend Metric.() -> RunObject): RunObject =
          block()

        override suspend fun event(message: String) {}

        override suspend fun parameter(key: String, value: String) {}

        override suspend fun parameter(key: String, values: List<String>) {}
      }
  }
}
