package com.xebia.functional.tokenizer

import kotlin.math.min

/**
 * Parameter for the byte pair encoding used to tokenize for the OpenAI GPT models.
 * This library supports the encodings that are listed in [EncodingType] out of the box.
 * But if you want to use a custom encoding, you can use this class to pass the parameters to the library.
 *
 * The encoding parameters are:
 *  * pattern: The pattern that is used to split the input text into tokens.
 *  * encoder: The encoder that maps the tokens to their ids.
 *  * specialTokensEncoder: The encoder that maps the special tokens to their ids.
 */
data class GptBytePairEncodingParams(
  val name: String,
  val regex: Regex,
  val encoder: Map<ByteArray, Int>,
  val specialTokensEncoder: Map<String, Int>
)

/**
 * Implementation of the byte pair encoding algorithm as used by the OpenAI tiktoken tokenizer.
 */
internal class GptBytePairEncoding(params: GptBytePairEncodingParams) : Encoding {
  override val name: String = params.name
  private val pattern: Regex = params.regex
  private val encoder: TokenEncoder<ImmutableByteArray, Int> =
    TokenEncoder(params.encoder, ImmutableByteArray.Companion::from)
  private val specialTokensEncoder: TokenEncoder<String, Int> = TokenEncoder(params.specialTokensEncoder)

  override fun encode(text: String?): List<Int> =
    encodeInternal(text, null).tokens

  override fun encode(text: String?, maxTokens: Int): EncodingResult =
    encodeInternal(text, maxTokens)

  private fun encodeInternal(text: String?, maxTokens: Int?): EncodingResult {
    if (text == null) return EncodingResult(emptyList(), false)
    for (specialToken in specialTokensEncoder.decodedTokens) {
      if (text.contains(specialToken)) throw UnsupportedOperationException("Encoding special tokens is not supported yet.")
    }
    return encodeOrdinaryInternal(text, maxTokens)
  }

  override fun encodeOrdinary(text: String): List<Int> =
    encodeOrdinaryInternal(text, null).tokens

  override fun encodeOrdinary(text: String, maxTokens: Int): EncodingResult =
    encodeOrdinaryInternal(text, maxTokens)

  private fun encodeOrdinaryInternal(text: String?, maxTokens: Int?): EncodingResult {
    if (text == null) return EncodingResult(emptyList(), false)
    val out = buildList {
      var tokenCount = 0
      pattern.findAll(text)
        .takeWhile { maxTokenCountNotReached(maxTokens, tokenCount) }
        .forEach { result ->
          val res = result.value.encodeToByteArray()
          val match = ImmutableByteArray.from(res)
          if (encoder.containsDecodedToken(match)) {
            add(encoder.encode(match))
            tokenCount++
          } else {
            val tokensToAdd = bytePairMerge(match)
            tokenCount += addTokens(this, tokensToAdd, maxTokens)
          }
        }
    }
    if (maxTokens != null) {
      // Make sure we didn't break the multibyte character
      for (tokensToRemove in 0..out.size) {
        val tokens: List<Int> = out.subList(0, out.size - tokensToRemove)
        val decoded = decode(tokens)
        if (text.startsWith(decoded)) {
          // If decoded text is equal to the head of the original text, we can safely return the tokens
          return EncodingResult(tokens, text.length > decoded.length)
        }
      }
    }
    return EncodingResult(out, false)
  }

  /**
   * Adds tokens from 'tokensToAdd' to 'out' until either 'maxTokens' is reached or 'tokensToAdd' is exhausted.
   *
   * @return the number of tokens added to 'out'
   */
  private fun addTokens(out: MutableList<Int>, tokensToAdd: List<Int>, maxTokens: Int?): Int {
    if (maxTokens != null) {
      val sublist = tokensToAdd.subList(0, min(maxTokens - out.size, tokensToAdd.size))
      out.addAll(sublist)
      return sublist.size
    }
    out.addAll(tokensToAdd)
    return tokensToAdd.size
  }

  override fun countTokens(text: String): Int =
    encode(text).size

  override fun countTokensOrdinary(text: String): Int =
    encodeOrdinary(text).size

  override fun decode(tokens: List<Int>): String =
    decodeBytes(tokens).decodeToString()

  override fun decodeBytes(tokens: List<Int>): ByteArray =
    buildList {
      for (token in tokens) {
        val decodedToken = decodeToken(token)
        for (b in decodedToken) {
          add(b)
        }
      }
    }.toByteArray()

  /*
  * We use a custom implementation of the byte pair encoding algorithm as used by the OpenAI tokenizer. The
  * piece is merged according to the merging rules provided by OpenAI. An example of the algorithm:
  *
  * piece:  v   e   c   t   o   r
  * index:  0   1   2   3   4   5   6
  * ranks:  4   3   7   2   13  inf inf
  *
  * We don't modify piece directly. We instead create a list of tuples (index, rank) where index is the start index
  * of a byte pair and rank is it's merge rank. We call this list of tuples parts. The lowest rank is the byte pair
  * that will be merged next. In the example above, the lowest rank is 2, so we merge the byte pair at index 3.
  * To merge a byte pair at index i, we first update the ranks of the byte pairs that are affected by the merge, in this
  * case the byte pair at index 2 and the byte pair at index 3. Then we remove the byte pair at index i + 1 from the list.
  * In this case, this is the byte pair at index 4.
  *
  * piece:  v   e   c   to   r
  * index:  0   1   2   3    5   6
  * ranks:  4   3   5   9    inf inf
  *
  * We then repeat the process until there are no more byte pairs to merge, either because we have merged all byte pairs
  * and parts.size() is 1, or because there are no more merging rules that apply to our tokens. Let's assume there are merging
  * rules for "e + c", "to + r" and "v + ec":
  *
  * piece:  v   ec  to   r
  * index:  0   1   3    5   6
  * ranks:  4   11  12   inf inf
  *         ^
  *
  * piece:  vec to   r
  * index:  0   3    5   6
  * ranks:  inf 12   inf inf
  *             ^
  *
  * piece:  vec tor
  * index:  0   3   6
  * ranks:  inf inf inf
  *
  * We can extract the final tokens by simply taking piece.get(parts[0].index) until piece.get(parts[1].index - 1)
  * and piece.get(parts[1].index) until piece.get(parts[2].index - 1). Analogously for more than two parts.
  * Note that we do not actually modify the piece, but only the parts list. The above visualization is just for
  * illustration purposes.
  */
  private fun bytePairMerge(piece: ImmutableByteArray): List<Int> {
    /*
     * piece:  v   e   c   t   o   r
     * index:  0   1   2   3   4   5   6
     * ranks:  inf inf inf inf inf inf inf
     */
    val parts = MutableList(piece.size + 1) { i -> PieceIndexToRank(i, Int.MAX_VALUE) }

    /*
     * piece:  v   e   c   t   o   r
     * index:  0   1   2   3   4   5   6
     * ranks:  4   3   7   2   13  inf inf
     */
    for (i in 0 until parts.size - 2) {
      getRank(piece, parts, i, 0)?.let { rank ->
        parts[i].rank = rank
      }
    }
    while (parts.size > 1) {
      /*
       * piece:  v   e   c   t   o   r
       * index:  0   1   2   3   4   5   6
       * ranks:  4   3   7   2   13  inf inf
       *
       * minRankIndex = 3
       * minRank = 2
       */
      var minRankIndex = 0
      var minRank = Int.MAX_VALUE
      for (i in 0 until parts.size - 1) {
        val rank = parts[i].rank
        if (rank < minRank) {
          minRank = rank
          minRankIndex = i
        }
      }

      /*
       * piece:  v   e   c   to   r
       * index:  0   1   2   3    5   6
       * ranks:  4   3   5   9    inf inf
       */
      if (minRank != Int.MAX_VALUE) {
        // Note that we calculate the rank of the byte pairs at minRankIndex and minRankIndex - 1 before removing
        // the merged byte pair. We use the skip parameter of the getRank function to calculate the rank of, in our
        // example, "t" + "o" + "r" and "c" + "t" + "o". The assumption made in the OpenAI implementation is that
        // removing first thrashes the cache, so it's better to calculate the rank of the byte pairs that are
        // affected by the merge before removing the merged byte pair. I did not verify, if this is actually the
        // case in java.
        parts[minRankIndex].rank = getRank(piece, parts, minRankIndex, 1) ?: Int.MAX_VALUE
        if (minRankIndex > 0) {
          parts[minRankIndex - 1].rank = getRank(piece, parts, minRankIndex - 1, 1) ?: Int.MAX_VALUE
        }
        parts.removeAt(minRankIndex + 1)
      } else {
        break
      }
    }

    /*
     * piece:  vec tor
     * index:  0   3   6
     * ranks:  inf inf inf
     */
    return buildList(parts.size) {
      for (i in 0 until parts.size - 1) {
        add(encoder.encode(piece.getBytesBetween(parts[i].index, parts[i + 1].index)))
      }
    }
  }

  private fun maxTokenCountReached(maxTokenCount: Int?, tokenCount: Int): Boolean =
    maxTokenCount != null && maxTokenCount <= tokenCount

  private fun maxTokenCountNotReached(maxTokenCount: Int?, tokenCount: Int): Boolean =
    !maxTokenCountReached(maxTokenCount, tokenCount)

  private fun getRank(
    piece: ImmutableByteArray,
    parts: List<PieceIndexToRank>,
    startIndex: Int,
    skip: Int
  ): Int? {
    if (startIndex + skip + 2 >= parts.size) return null
    val pieceStartIndex = parts[startIndex].index
    val pieceEndIndex = parts[startIndex + skip + 2].index
    val encoderIndex = piece.getBytesBetween(pieceStartIndex, pieceEndIndex)
    return encoder.encodeIfPresent(encoderIndex)
  }

  private fun decodeToken(token: Int): ByteArray =
    encoder.decodeIfPresent(token)?.rawArray ?: specialTokensEncoder.decodeIfPresent(token)?.encodeToByteArray()
    ?: throw IllegalArgumentException("Unknown token for decoding: $token")
}

private class PieceIndexToRank(val index: Int, var rank: Int)
