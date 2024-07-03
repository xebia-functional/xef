package com.xebia.functional.xef.metrics

import com.xebia.functional.xef.openapi.MessageObject
import com.xebia.functional.xef.openapi.RunObject
import com.xebia.functional.xef.openapi.RunStepObject
import com.xebia.functional.xef.prompt.Prompt

interface Metric {
  suspend fun <A> customSpan(
    name: String,
    parameters: Map<String, String>,
    block: suspend Metric.() -> A
  ): A

  suspend fun <A> promptSpan(prompt: Prompt, block: suspend Metric.() -> A): A

  suspend fun event(message: String)

  suspend fun parameter(key: String, value: String)

  suspend fun parameter(key: String, values: List<String>)

  suspend fun assistantCreateRun(runObject: RunObject, source: String)

  suspend fun assistantCreateRunStep(runObject: RunStepObject, source: String)

  suspend fun assistantCreatedMessage(messageObject: MessageObject, source: String)

  companion object {
    val EMPTY: Metric =
      object : Metric {
        override suspend fun <A> customSpan(
          name: String,
          parameters: Map<String, String>,
          block: suspend Metric.() -> A
        ): A = block()

        override suspend fun <A> promptSpan(prompt: Prompt, block: suspend Metric.() -> A): A =
          block()

        override suspend fun assistantCreateRun(runObject: RunObject, source: String) {}

        override suspend fun assistantCreateRunStep(runObject: RunStepObject, source: String) {}

        override suspend fun assistantCreatedMessage(
          messageObject: MessageObject,
          source: String
        ) {}

        override suspend fun event(message: String) {}

        override suspend fun parameter(key: String, value: String) {}

        override suspend fun parameter(key: String, values: List<String>) {}
      }
  }
}
