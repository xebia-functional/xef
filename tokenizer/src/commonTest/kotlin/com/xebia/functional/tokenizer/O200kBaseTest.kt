package com.xebia.functional.tokenizer

import com.goncalossilva.resources.Resource
import com.xebia.functional.tokenizer.EncodingType.O200K_BASE
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlin.test.Test

// Ignore test on native since not all characters can be encoded
@IgnoreOnNative
class O200kBaseTest {
  private val resource = Resource("src/commonTest/resources/o200k_base_encodings.csv")
  private val ENCODING = O200K_BASE.encoding

  @Test
  fun o200kBaseEncodesCorrectly() {
    resource.splitCSV().forEach { (input, output, _) ->
      ENCODING.encode(input) shouldBe output.parseEncoding()
    }
  }

  @Test
  fun o200kBaseEncodesStable() {
    resource.splitCSV().forEach { (input, _, _) ->
      ENCODING.decode(ENCODING.encode(input)) shouldBe input
    }
  }

  @Test
  fun o200kBaseEncodesCorrectlyWithMaxTokensSet() {
    resource.splitCSV().forEach { (input, output, outputMaxTokens10) ->
      val expected = output.parseEncoding()
      val expectedWithMaxTokens = outputMaxTokens10.parseEncoding()
      val encodingResult = ENCODING.encode(input, 10)
      encodingResult.tokens shouldBe expectedWithMaxTokens
      (expected.size > expectedWithMaxTokens.size) shouldBe encodingResult.isTruncated
    }
  }

  @Test
  fun o200kBaseEncodesStableWithMaxTokensSet() {
    resource.splitCSV().forEach { (input, _, _) ->
      val actual = ENCODING.decode(ENCODING.encode(input, 10).tokens)
      input shouldStartWith actual
    }
  }

  @Test
  fun o200kBaseEncodeOrdinaryEncodesCorrectly() {
    resource.splitCSV().forEach { (input, output, _) ->
      ENCODING.encodeOrdinary(input) shouldBe output.parseEncoding()
    }
  }

  @Test
  fun o200kBaseEncodeOrdinaryEncodesCorrectlyWithMaxTokens() {
    resource.splitCSV().forEach { (input, output, outputMaxTokens10) ->
      val expected = output.parseEncoding()
      val expectedWithMaxTokens = outputMaxTokens10.parseEncoding()
      val encodingResult = ENCODING.encodeOrdinary(input, 10)
      encodingResult.tokens shouldBe expectedWithMaxTokens
      (expected.size > expectedWithMaxTokens.size) shouldBe encodingResult.isTruncated
    }
  }

  @Test
  fun o200kBaseEncodeOrdinaryEncodesStable() {
    resource.splitCSV().forEach { (input, _, _) ->
      ENCODING.decode(ENCODING.encodeOrdinary(input)) shouldBe input
    }
  }

  @Test
  fun o200kBaseEncodeOrdinaryEncodesStableWithMaxTokensSet() {
    resource.splitCSV().forEach { (input, _, _) ->
      val actual = ENCODING.decode(ENCODING.encodeOrdinary(input, 10).tokens)
      input shouldStartWith actual
    }
  }

  @Test
  fun o200kBaseEncodeOrdinaryEncodesSpecialTokensCorrectly() {
    val input = "Hello<|endoftext|>, world ! <|endofprompt|>"
    val actual = ENCODING.decode(ENCODING.encodeOrdinary(input))
    actual shouldBe input
  }
}
