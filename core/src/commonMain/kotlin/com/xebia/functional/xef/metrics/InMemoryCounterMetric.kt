package com.xebia.functional.xef.metrics

import arrow.atomic.AtomicLong

class InMemoryCounterMetric : CounterMetric {
  private val count = AtomicLong(0)

  override fun increment(n: Long) {
    count.incrementAndGet()
  }

  override fun decrement(n: Long) {
    count.decrementAndGet()
  }
}
