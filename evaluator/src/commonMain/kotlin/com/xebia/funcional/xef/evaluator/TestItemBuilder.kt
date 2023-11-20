package com.xebia.funcional.xef.evaluator

import com.xebia.funcional.xef.evaluator.models.ContextDescription
import com.xebia.funcional.xef.evaluator.models.OutputResponse

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
