package com.xebia.functional.tokenizer

import com.goncalossilva.resources.Resource
import io.kotest.assertions.withClue
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import kotlin.test.Test

class EncodingTest {
  private val resource = Resource("src/commonTest/resources/cl100k_base_encodings.csv")
  private val ENCODING = EncodingType.CL100K_BASE.encoding

  @Test
  fun truncateText() {
    resource.splitCSV().forEach { (input, _, _) ->
      val result = ENCODING.truncateText(input, 10)
      ENCODING.countTokens(result) shouldBeLessThanOrEqual 10
    }
  }
}
