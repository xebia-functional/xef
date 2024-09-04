package com.xebia.functional.xef.opentelemetry

import com.xebia.functional.xef.metrics.CounterMetric
import io.opentelemetry.api.common.Attributes

class OpenTelemetryCounter(private val longCounter: io.opentelemetry.api.metrics.LongCounter) :
  CounterMetric {
  override fun increment(n: Long) {
    longCounter.add(n)
  }

  override fun increment(n: Long, attributes: Map<String, String>) {
    val attributesBuilder = Attributes.builder()
    attributes.forEach { (k, v) -> attributesBuilder.put(k, v) }
    longCounter.add(n, attributesBuilder.build())
  }
}
