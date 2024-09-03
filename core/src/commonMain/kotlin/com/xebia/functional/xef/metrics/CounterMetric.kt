package com.xebia.functional.xef.metrics

interface CounterMetric {
  fun increment(n: Long)

  fun decrement(n: Long)
}
