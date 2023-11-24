package com.xebia.functional.xef.opentelemetry

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context

class OpenTelemetryState(val tracer: Tracer) {

  val contextSpans = mutableListOf<Context>()

  suspend fun <A> span(name: String, block: suspend OpenTelemetryState.(Span) -> A): A {
    val currentContext = contextSpans.lastOrNull()

    val currentSpan =
      currentContext?.let {
        val span = tracer.spanBuilder(name).setParent(it).startSpan()
        span.makeCurrent()
        contextSpans.add(Context.current())
        span
      }
        ?: run {
          val span = tracer.spanBuilder(name).startSpan()
          span.makeCurrent()
          contextSpans.add(Context.current())
          span
        }
    val output = block(currentSpan)

    currentSpan.end()

    return output
  }

  fun event(name: String) {
    val currentContext = contextSpans.lastOrNull()
    val currentContextSpan = currentContext?.let { Span.fromContext(it) }
    currentContextSpan?.addEvent(name)
  }

  fun setAttribute(key: String, value: String) {
    val currentContext = contextSpans.lastOrNull()
    val currentContextSpan = currentContext?.let { Span.fromContext(it) }

    currentContextSpan?.let {
      Context.current().with(it)
      it.setAttribute(key, value)
    }
  }
}
