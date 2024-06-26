package com.xebia.functional.xef.opentelemetry

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.extension.kotlin.asContextElement
import io.opentelemetry.extension.kotlin.getOpenTelemetryContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext

class OpenTelemetryState(private val tracer: Tracer) {

  suspend fun <A> span(
    name: String,
    parameters: Map<String, String>,
    block: suspend (Span) -> A
  ): A {
    val parentOrRoot = currentCoroutineContext().getOpenTelemetryContext()

    val currentSpan =
      tracer.spanBuilder(name).setParent(parentOrRoot).setSpanKind(SpanKind.CLIENT).startSpan()

    return try {
      withContext(currentSpan.asContextElement()) {
        currentSpan.makeCurrent().use {
          parameters.map { (key, value) -> currentSpan.setAttribute(key, value) }
          block(currentSpan)
        }
      }
    } finally {
      currentSpan.end()
    }
  }

  suspend fun event(name: String) {
    currentCoroutineContext().getOpenTelemetryContext().let(Span::fromContext).addEvent(name)
  }

  suspend fun setAttribute(key: String, value: String) {
    currentCoroutineContext()
      .getOpenTelemetryContext()
      .let(Span::fromContext)
      .setAttribute(key, value)
  }

  suspend fun setAttribute(key: String, values: List<String>) {
    currentCoroutineContext()
      .getOpenTelemetryContext()
      .let(Span::fromContext)
      .setAttribute(AttributeKey.stringArrayKey(key), values)
  }
}
