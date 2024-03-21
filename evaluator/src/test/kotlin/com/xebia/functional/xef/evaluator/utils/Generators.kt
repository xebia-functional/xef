package com.xebia.functional.xef.evaluator.utils

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.az
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.stringPattern

object Generators {
  val simpleString = Arb.string(1, 60, Codepoint.az())
  val emptyString = Arb.stringPattern("\\s+")
}
