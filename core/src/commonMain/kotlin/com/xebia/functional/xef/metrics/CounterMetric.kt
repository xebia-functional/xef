package com.xebia.functional.xef.metrics

interface CounterMetric {
  fun increment(n: Long)

  fun increment(n: Long, attributes: Map<String, String>)
}
