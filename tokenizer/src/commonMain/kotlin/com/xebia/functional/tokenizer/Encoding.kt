package com.xebia.functional.tokenizer

import kotlin.math.roundToInt

/** The result of encoding operation. */
data class EncodingResult(
  val tokens: List<Int>,
  /** if the token list was truncated because the maximum token length was exceeded */
  val isTruncated: Boolean
)


interface Encoding {

  val name: String

  /**
   * Encodes the given text into a list of token ids.
   *
   * Special tokens are artificial tokens used to unlock capabilities from a model,
   * such as fill-in-the-middle. There is currently no support for parsing special tokens
   * in a text, so if the text contains special tokens, this method will throw an
   * [UnsupportedOperationException].
   *
   *
   * If you want to encode special tokens as ordinary text, use [Encoding.encodeOrdinary].
   *
   * ```kotlin
   * val encoding = EncodingType.CL100K_BASE.encoding
   * encoding.encode("hello world")
   * // returns [15339, 1917]
   *
   * encoding.encode("hello &lt;|endoftext|&gt; world")
   * // raises an UnsupportedOperationException
   * ```
   *
   * @param text the text to encode
   * @return the list of token ids
   * @throws UnsupportedOperationException if the text contains special tokens which are not supported for now
   */
  fun encode(text: String?): List<Int>

  /**
   * Encodes the given text into a list of token ids.
   *
   *
   * Special tokens are artificial tokens used to unlock capabilities from a model,
   * such as fill-in-the-middle. There is currently no support for parsing special tokens
   * in a text, so if the text contains special tokens, this method will throw an
   * [UnsupportedOperationException].
   *
   * If you want to encode special tokens as ordinary text, use [Encoding.encodeOrdinary].
   *
   *
   * This method will truncate the list of token ids if the number of tokens exceeds the
   * given maxTokens parameter. Note that it will try to keep characters together, that are encoded into
   * multiple tokens. For example, if the text contains a character which is encoded into 3 tokens,
   * and due to the maxTokens parameter the last token of the character is truncated, the first two
   * tokens of the character will also be truncated. Therefore, the actual number of tokens may be
   * less than the given maxTokens parameter.
   *
   * ```kotlin
   * val encoding = EncodingType.CL100K_BASE.encoding
   * encoding.encode("hello world", 100)
   * // returns [15339, 1917]
   *
   * encoding.encode("hello &lt;|endoftext|&gt; world", 100)
   * // raises an UnsupportedOperationException
   * ```
   *
   * @param text the text to encode
   * @param maxTokens the maximum number of tokens to encode
   * @return the [EncodingResult] containing a list of token ids and whether the tokens were truncated due to the maxTokens parameter
   * @throws UnsupportedOperationException if the text contains special tokens which are not supported for now
   */
  fun encode(text: String?, maxTokens: Int): EncodingResult

  /**
   * Encodes the given text into a list of token ids, ignoring special tokens.
   *
   * This method does not throw an exception if the text contains special tokens, but instead
   * encodes them as if they were ordinary text.
   *
   * ```kotlin
   * val encoding = EncodingType.CL100K_BASE.encoding
   * encoding.encodeOrdinary("hello world");
   * // returns [15339, 1917]
   *
   * encoding.encodeOrdinary("hello &lt;|endoftext|&gt; world");
   * // returns [15339, 83739, 8862, 728, 428, 91, 29, 1917]
   * ```
   *
   * @param text the text to encode
   * @return the list of token ids
   */
  fun encodeOrdinary(text: String): List<Int>

  /**
   * Encodes the given text into a list of token ids, ignoring special tokens.
   *
   *
   * This method does not throw an exception if the text contains special tokens, but instead
   * encodes them as if they were ordinary text.
   *
   * It will truncate the list of token ids if the number of tokens exceeds the
   * given maxTokens parameter. Note that it will try to keep characters together, that are encoded into
   * multiple tokens. For example, if the text contains a character which is encoded into 3 tokens,
   * and due to the maxTokens parameter the last token of the character is truncated, the first two
   * tokens of the character will also be truncated. Therefore, the actual number of tokens may be
   * less than the given maxTokens parameter.
   *
   * ```kotlin
   * val encoding = EncodingType.CL100K_BASE.encoding
   * encoding.encodeOrdinary("hello world", 100);
   * // returns [15339, 1917]
   *
   * encoding.encodeOrdinary("hello &lt;|endoftext|&gt; world", 100);
   * // returns [15339, 83739, 8862, 728, 428, 91, 29, 1917]
   * ```
   *
   * @param text the text to encode
   * @param maxTokens the maximum number of tokens to encode
   * @return the [EncodingResult] containing a list of token ids and whether the tokens were truncated due to the maxTokens parameter
   */
  fun encodeOrdinary(text: String, maxTokens: Int): EncodingResult

  /**
   * Encodes the given text into a list of token ids and returns the amount of tokens.
   * This is a convenience method for [.encode], if all you want is to
   * know the amount of tokens. It is not more performant than [.encode],
   * so prefer to use [.encode] if you actually need the tokens.
   *
   * ```kotlin
   * val encoding = EncodingType.CL100K_BASE.encoding
   * encoding.countTokens("hello world");
   * // returns 2
   *
   * encoding.countTokens("hello &lt;|endoftext|&gt; world");
   * // raises an UnsupportedOperationException
   * ```
   *
   * @param text the text to count tokens for
   * @return the amount of tokens
   * @throws UnsupportedOperationException if the text contains special tokens which are not supported for now
   */
  fun countTokens(text: String): Int

  /**
   * Encodes the given text into a list of token ids and returns the amount of tokens.
   * This is a convenience method for [.encodeOrdinary], if all you want is to
   * know the amount of tokens. It is not more performant than [.encodeOrdinary],
   * so prefer to use [.encodeOrdinary] if you actually need the tokens.
   *
   * ```kotlin
   * val encoding = EncodingType.CL100K_BASE.encoding
   * encoding.countTokensOrdinary("hello world");
   * // returns 2
   *
   * encoding.countTokensOrdinary("hello &lt;|endoftext|&gt; world");
   * // returns 8
   * ```
   *
   * @param text the text to count tokens for
   * @return the amount of tokens
   * @throws UnsupportedOperationException if the text contains special tokens which are not supported for now
   */
  fun countTokensOrdinary(text: String): Int

  /**
   * Decodes the given list of token ids into a text.
   *
   * ```kotlin
   * val encoding = EncodingType.CL100K_BASE.encoding
   * encoding.decode(List.of(15339, 1917));
   * // returns "hello world"
   *
   * encoding.decode(List.of(15339, 1917, Integer.MAX_VALUE));
   * // raises an IllegalArgumentException
   * ```
   *
   * @param tokens the list of token ids
   * @return the decoded text
   * @throws IllegalArgumentException if the list contains invalid token ids
   */
  fun decode(tokens: List<Int>): String

  /**
   * Decodes the given list of token ids into a [ByteArray].
   *
   * ```kotlin
   * val encoding = EncodingType.CL100K_BASE.encoding
   * encoding.decodeBytes(listOf(15339, 1917));
   * // returns [104, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100]
   *
   * encoding.decodeBytes(List.of(15339, 1917, Integer.MAX_VALUE));
   * // raises an IllegalArgumentException
   * ```
   * @param tokens the list of token ids
   * @return the decoded byte array
   * @throws IllegalArgumentException if the list contains invalid token ids
   */
  fun decodeBytes(tokens: List<Int>): ByteArray
}

/**
 * Truncates the given [text] to the given [maxTokens] by removing tokens from the end of the text.
 * It removes tokens from the tail of the [text].
 * Tokens are chosen to be removed based on the percentage of the [maxTokens]
 * compared to the total amount of tokens in the [text].
 *
 * If the truncation fails,
 * it will retry by recursively calling this function until a text with maxTokens is found.
 *
 * **WARNING:** for small [maxTokens] this function may hang forever,
 * some [text] like emoticons, or special characters have token length of 9.
 * So trying to truncateText to maxToken = 5 might hang forever for them.
 *
 * **WARNING:** This method might truncate crucial information from your prompt,
 * and as a result might degrade reliability of your prompts.
 */
tailrec fun Encoding.truncateText(text: String, maxTokens: Int): String {
  val tokenCount = countTokens(text)
  return if (tokenCount <= maxTokens) text
  else {
    val percentage = maxTokens.toDouble() / tokenCount.toDouble()
    val truncatedTextLength = (text.length * percentage).roundToInt()
    val result = text.substring(0, truncatedTextLength)
    val tokenCountResult = countTokens(result)
    when {
      tokenCountResult >= maxTokens -> truncateText(result, maxTokens)
      else -> result
    }
  }
}
