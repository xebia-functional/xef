package com.xebia.functional.tokenizer

import com.goncalossilva.resources.Resource
import com.xebia.functional.tokenizer.EncodingType.CL100K_BASE
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlin.test.Test

class Cl100kBaseTest {
  private val resource = Resource("src/commonTest/resources/cl100k_base_encodings.csv")
  private val ENCODING = CL100K_BASE.encoding

  @Test
  fun cl100kBaseEncodesCorrectly() {
    resource.splitCSV().forEach { (input, output, _) ->
      ENCODING.encode(input) shouldBe output.parseEncoding()
    }
  }

  @Test
  fun cl100kBaseEncodesStable() {
    resource.splitCSV().forEach { (input, _, _) ->
      ENCODING.decode(ENCODING.encode(input)) shouldBe input
    }
  }

  @Test
  fun cl100kBaseEncodesCorrectlyWithMaxTokensSet() {
    resource.splitCSV().forEach { (input, output, outputMaxTokens10) ->
      val expected = output.parseEncoding()
      val expectedWithMaxTokens = outputMaxTokens10.parseEncoding()
      val encodingResult = ENCODING.encode(input, 10)
      encodingResult.tokens shouldBe expectedWithMaxTokens
      (expected.size > expectedWithMaxTokens.size) shouldBe encodingResult.isTruncated
    }
  }

  @Test
  fun cl100kBaseEncodesStableWithMaxTokensSet() {
    resource.splitCSV().forEach { (input, _, _) ->
      val actual = ENCODING.decode(ENCODING.encode(input, 10).tokens)
      input shouldStartWith actual
    }
  }

  @Test
  fun cl100kBaseEncodeOrdinaryEncodesCorrectly() {
    resource.splitCSV().forEach { (input, output, _) ->
      ENCODING.encodeOrdinary(input) shouldBe output.parseEncoding()
    }
  }

  @Test
  fun cl100kBaseEncodeOrdinaryEncodesCorrectlyWithMaxTokens() {
    resource.splitCSV().forEach { (input, output, outputMaxTokens10) ->
      val expected = output.parseEncoding()
      val expectedWithMaxTokens = outputMaxTokens10.parseEncoding()
      val encodingResult = ENCODING.encodeOrdinary(input, 10)
      encodingResult.tokens shouldBe expectedWithMaxTokens
      (expected.size > expectedWithMaxTokens.size) shouldBe encodingResult.isTruncated
    }
  }

  @Test
  fun cl100kBaseEncodeOrdinaryEncodesStable() {
    resource.splitCSV().forEach { (input, _, _) ->
      ENCODING.decode(ENCODING.encodeOrdinary(input)) shouldBe input
    }
  }

  @Test
  fun cl100kBaseEncodeOrdinaryEncodesStableWithMaxTokensSet() {
    resource.splitCSV().forEach { (input, _, _) ->
      val actual = ENCODING.decode(ENCODING.encodeOrdinary(input, 10).tokens)
      input shouldStartWith actual
    }
  }

  @Test
  fun cl100kBaseEncodeOrdinaryEncodesSpecialTokensCorrectly() {
    val input = "Hello<|endoftext|>, <|fim_prefix|> <|fim_middle|> world <|fim_suffix|> ! <|endofprompt|>"
    val actual = ENCODING.decode(ENCODING.encodeOrdinary(input))
    actual shouldBe input
  }
}
