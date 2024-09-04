package com.xebia.functional.xef.metrics

import arrow.atomic.AtomicLong
import io.github.oshai.kotlinlogging.KLogger

class InMemoryCounterMetric(val name: String, val logger: KLogger) : CounterMetric {
  private val count = AtomicLong(0)

  override fun increment(n: Long) {
    count.addAndGet(n)
    logger.info { "Counter $name incremented to ${count.get()}" }
  }

  override fun increment(n: Long, attributes: Map<String, String>) {
    count.addAndGet(n)
    logger.info {
      "Counter $name incremented to ${count.get()} with those attributes: ${attributes.entries.joinToString( ",")}"
    }
  }
}
