package com.xebia.functional.xef.evaluator.models

sealed interface MetricValues

sealed interface Metric {
  fun template(input: String, output: String, context: String, metricValues: MetricValues): String
}
