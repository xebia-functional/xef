package com.xebia.functional.xef.metrics

import com.xebia.functional.xef.openapi.MessageObject
import com.xebia.functional.xef.openapi.RunObject
import com.xebia.functional.xef.openapi.RunStepObject
import com.xebia.functional.xef.prompt.Prompt

interface Metric {
  suspend fun <A> customSpan(name: String, block: suspend Metric.() -> A): A

  suspend fun <A> promptSpan(prompt: Prompt, block: suspend Metric.() -> A): A

  suspend fun event(message: String)

  suspend fun parameter(key: String, value: String)

  suspend fun parameter(key: String, values: List<String>)

  suspend fun assistantCreateRun(runObject: RunObject)

  suspend fun assistantCreateRun(runId: String, block: suspend Metric.() -> RunObject): RunObject

  suspend fun assistantCreateRunStep(runObject: RunStepObject)

  suspend fun assistantCreatedMessage(
    runId: String,
    block: suspend Metric.() -> List<MessageObject>
  ): List<MessageObject>

  suspend fun assistantCreateRunStep(
    runId: String,
    block: suspend Metric.() -> RunStepObject
  ): RunStepObject

  suspend fun assistantToolOutputsRun(
    runId: String,
    block: suspend Metric.() -> RunObject
  ): RunObject

  companion object {
    val EMPTY: Metric =
      object : Metric {
        override suspend fun <A> customSpan(name: String, block: suspend Metric.() -> A): A =
          block()

        override suspend fun <A> promptSpan(prompt: Prompt, block: suspend Metric.() -> A): A =
          block()

        override suspend fun assistantCreateRun(runObject: RunObject) {}

        override suspend fun assistantCreateRun(
          runId: String,
          block: suspend Metric.() -> RunObject
        ): RunObject = block()

        override suspend fun assistantCreateRunStep(runObject: RunStepObject) {}

        override suspend fun assistantCreatedMessage(
          runId: String,
          block: suspend Metric.() -> List<MessageObject>
        ): List<MessageObject> = block()

        override suspend fun assistantCreateRunStep(
          runId: String,
          block: suspend Metric.() -> RunStepObject
        ): RunStepObject = block()

        override suspend fun assistantToolOutputsRun(
          runId: String,
          block: suspend Metric.() -> RunObject
        ): RunObject = block()

        override suspend fun event(message: String) {}

        override suspend fun parameter(key: String, value: String) {}

        override suspend fun parameter(key: String, values: List<String>) {}
      }
  }
}
