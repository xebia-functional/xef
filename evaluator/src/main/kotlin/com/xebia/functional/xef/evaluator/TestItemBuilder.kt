package com.xebia.functional.xef.evaluator

import com.xebia.functional.xef.evaluator.models.ContextDescription
import com.xebia.functional.xef.evaluator.models.OutputResponse

class TestItemBuilder(val input: String) {

  private lateinit var context: String

  private val outputs = mutableListOf<String>()

  operator fun ContextDescription.unaryPlus() {
    context = value
  }

  operator fun OutputResponse.unaryPlus() {
    outputs.add(value)
  }

  fun build() = ItemSpec(input, context, outputs)
}
