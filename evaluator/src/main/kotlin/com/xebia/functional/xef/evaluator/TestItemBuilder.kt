package com.xebia.functional.xef.evaluator

import com.xebia.functional.xef.evaluator.models.ContextDescription
import com.xebia.functional.xef.evaluator.models.OutputResponse

class TestItemBuilder(val input: String) {

  private val context = mutableListOf<String>()

  private val outputs = mutableListOf<String>()

  operator fun ContextDescription.unaryPlus() {
    context.add(value)
  }

  operator fun OutputResponse.unaryPlus() {
    outputs.add(value)
  }

  fun build() = TestSpecItem(input, context, outputs)
}
