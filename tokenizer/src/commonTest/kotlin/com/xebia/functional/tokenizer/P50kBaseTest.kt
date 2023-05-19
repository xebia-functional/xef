package com.xebia.functional.tokenizer

import com.goncalossilva.resources.Resource
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlin.test.Test

class P50kBaseTest {
  private val resource = Resource("src/commonTest/resources/p50k_base_encodings.csv")
  private val ENCODING = EncodingType.P50K_BASE.encoding

  @Test
  fun p50kBaseEncodesCorrectly() {
    resource.splitCSV().forEach { (input, output, _) ->
      ENCODING.encode(input) shouldBe output.parseEncoding()
    }
  }

  @Test
  fun p50kBaseBaseEncodesStable() {
    resource.splitCSV().forEach { (input, _, _) ->
      ENCODING.decode(ENCODING.encode(input)) shouldBe input
    }
  }

  @Test
  fun p50kBaseBaseEncodesCorrectlyWithMaxTokensSet() {
    resource.splitCSV().forEach { (input, output, outputMaxTokens10) ->
      val expected = output.parseEncoding()
      val expectedWithMaxTokens = outputMaxTokens10.parseEncoding()
      val encodingResult = ENCODING.encode(input, 10)
      encodingResult.tokens shouldBe expectedWithMaxTokens
      encodingResult.isTruncated shouldBe (expected.size > expectedWithMaxTokens.size)
    }
  }

  @Test
  fun p50kBaseBaseEncodesStableWithMaxTokensSet() {
    resource.splitCSV().forEach { (input, _, _) ->
      val actual = ENCODING.decode(ENCODING.encode(input, 10).tokens)
      input shouldStartWith actual
    }
  }

  @Test
  fun p50kBaseBaseEncodeOrdinaryEncodesCorrectly() {
    resource.splitCSV().forEach { (input, output, _) ->
      ENCODING.encodeOrdinary(input) shouldBe output.parseEncoding()
    }
  }

  @Test
  fun p50kBaseBaseEncodeOrdinaryEncodesCorrectlyWithMaxTokens() {
    resource.splitCSV().forEach { (input, output, outputMaxTokens10) ->
      val expected: List<Int> = output.parseEncoding()
      val expectedWithMaxTokens: List<Int> = outputMaxTokens10.parseEncoding()
      val encodingResult = ENCODING.encodeOrdinary(input, 10)
      encodingResult.tokens shouldBe expectedWithMaxTokens
      encodingResult.isTruncated shouldBe (expected.size > expectedWithMaxTokens.size)
    }
  }

  @Test
  fun p50kBaseBaseEncodeOrdinaryEncodesStable() {
    resource.splitCSV().forEach { (input, _, _) ->
      ENCODING.decode(ENCODING.encodeOrdinary(input)) shouldBe input
    }
  }

  @Test
  fun p50kBaseBaseEncodeOrdinaryEncodesStableWithMaxTokensSet() {
    resource.splitCSV().forEach { (input, _, _) ->
      val actual = ENCODING.decode(ENCODING.encodeOrdinary(input, 10).tokens)
      input shouldStartWith actual
    }
  }

  @Test
  fun p50kBaseBaseEncodeOrdinaryEncodesSpecialTokensCorrectly() {
    val input = "Hello<|endoftext|>, <|fim_prefix|> <|fim_middle|> world <|fim_suffix|> ! <|endofprompt|>"
    val actual = ENCODING.decode(ENCODING.encodeOrdinary(input))
    actual shouldBe input
  }
}