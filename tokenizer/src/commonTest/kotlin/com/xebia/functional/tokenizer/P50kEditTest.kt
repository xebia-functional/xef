package com.xebia.functional.tokenizer

import com.goncalossilva.resources.Resource
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlin.test.Test

class P50kEditTest {
  private val resource = Resource("src/commonTest/resources/p50k_edit_encodings.csv")
  private val ENCODING = EncodingType.P50K_EDIT.encoding

  @Test
  fun p50kEditEncodesCorrectly() {
    resource.splitCSV().forEach { (input, output, _) ->
      ENCODING.encode(input) shouldBe output.parseEncoding()
    }
  }

  @Test
  fun p50kEditEncodesStable() {
    resource.splitCSV().forEach { (input, _, _) ->
      ENCODING.decode(ENCODING.encode(input)) shouldBe input
    }
  }

  @Test
  fun p50kEditEncodesCorrectlyWithMaxTokensSet() {
    resource.splitCSV().forEach { (input, output, outputMaxTokens10) ->
      val expected: List<Int> = output.parseEncoding()
      val expectedWithMaxTokens: List<Int> = outputMaxTokens10.parseEncoding()
      val encodingResult: EncodingResult = ENCODING.encode(input, 10)
      encodingResult.tokens shouldBe expectedWithMaxTokens
      encodingResult.isTruncated shouldBe (expected.size > expectedWithMaxTokens.size)
    }
  }

  @Test
  fun p50kEditEncodesStableWithMaxTokensSet() {
    resource.splitCSV().forEach { (input, _, _) ->
      val actual = ENCODING.decode(ENCODING.encode(input, 10).tokens)
      input shouldStartWith actual
    }
  }

  @Test
  fun p50kEditEncodeOrdinaryEncodesCorrectly() {
    resource.splitCSV().forEach { (input, output, _) ->
      ENCODING.encodeOrdinary(input) shouldBe output.parseEncoding()
    }
  }

  @Test
  fun p50kEditEncodeOrdinaryEncodesCorrectlyWithMaxTokens() {
    resource.splitCSV().forEach { (input, output, outputMaxTokens10) ->
      val expected: List<Int> = output.parseEncoding()
      val expectedWithMaxTokens: List<Int> = outputMaxTokens10.parseEncoding()
      val encodingResult: EncodingResult = ENCODING.encodeOrdinary(input, 10)
      encodingResult.tokens shouldBe expectedWithMaxTokens
      encodingResult.isTruncated shouldBe (expected.size > expectedWithMaxTokens.size)
    }
  }

  @Test
  fun p50kEditEncodeOrdinaryEncodesStable() {
    resource.splitCSV().forEach { (input, _, _) ->
      ENCODING.decode(ENCODING.encodeOrdinary(input)) shouldBe input
    }
  }

  @Test
  fun p50kEditEncodeOrdinaryEncodesStableWithMaxTokensSet() {
    resource.splitCSV().forEach { (input, _, _) ->
      val actual = ENCODING.decode(ENCODING.encodeOrdinary(input, 10).tokens)
      input shouldStartWith actual
    }
  }

  @Test
  fun p50kEditEncodeOrdinaryEncodesSpecialTokensCorrectly() {
    val input = "Hello<|endoftext|>, <|fim_prefix|> <|fim_middle|> world <|fim_suffix|> ! <|endofprompt|>"
    ENCODING.decode(ENCODING.encodeOrdinary(input)) shouldBe input
  }
}