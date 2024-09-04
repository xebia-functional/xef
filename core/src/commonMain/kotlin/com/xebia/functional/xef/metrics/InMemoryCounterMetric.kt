package com.xebia.functional.xef.metrics

import arrow.atomic.AtomicLong
import io.github.oshai.kotlinlogging.KLogger

class InMemoryCounterMetric(val name: String, val logger: KLogger) : CounterMetric {
  private val count = AtomicLong(0)

  override fun increment(n: Long) {
    count.incrementAndGet()
    logger.info { "Counter $name incremented to ${count.get()}" }
  }

  override fun decrement(n: Long) {
    count.decrementAndGet()
    logger.info { "Counter $name decremented to ${count.get()}" }
  }
}
