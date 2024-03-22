package com.xebia.functional.xef.evaluator

import com.xebia.functional.xef.evaluator.models.ContextDescription
import com.xebia.functional.xef.evaluator.models.OutputResponse

class TestItemBuilder(val input: String) {

  private lateinit var context: String

  private val outputs = mutableListOf<OutputResponse>()

  operator fun ContextDescription.unaryPlus() {
    context = value
  }

  operator fun OutputResponse.unaryPlus() {
    outputs.add(this)
  }

  fun build() = ItemSpec(input, context, outputs)
}
