package com.xebia.functional.xef.opentelemetry

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import io.opentelemetry.extension.kotlin.getOpenTelemetryContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext

class OpenTelemetryState(val tracer: Tracer) {

  suspend fun <A> span(name: String, block: suspend OpenTelemetryState.(Span) -> A): A {
    val parentOrRoot = currentCoroutineContext().getOpenTelemetryContext()

    val currentSpan = tracer.spanBuilder(name).setParent(parentOrRoot).startSpan()

    return try {
      withContext(currentSpan.asContextElement()) {
        currentSpan.makeCurrent().use { block(currentSpan) }
      }
    } finally {
      currentSpan.end()
    }
  }

  suspend fun event(name: String) {
    currentCoroutineContext().getOpenTelemetryContext().let(Span::fromContext).addEvent(name)
  }

  suspend fun setAttribute(key: String, value: String) {
    val span = currentCoroutineContext().getOpenTelemetryContext().let(Span::fromContext)

    Context.current().with(span)
    span.setAttribute(key, value)
  }
}
