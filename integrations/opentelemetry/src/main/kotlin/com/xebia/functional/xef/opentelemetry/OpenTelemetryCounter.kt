package com.xebia.functional.xef.opentelemetry

import com.xebia.functional.xef.metrics.CounterMetric

class OpenTelemetryCounter(private val longCounter: io.opentelemetry.api.metrics.LongCounter) :
  CounterMetric {
  override fun increment(n: Long) {
    longCounter.add(n)
  }

  override fun decrement(n: Long) {
    longCounter.add(n)
  }
}
