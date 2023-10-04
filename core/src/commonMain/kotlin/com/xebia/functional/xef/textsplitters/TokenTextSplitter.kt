package com.xebia.functional.xef.textsplitters

import com.xebia.functional.tokenizer.Encoding
import com.xebia.functional.tokenizer.EncodingType

fun TokenTextSplitter(encodingType: EncodingType, chunkSize: Int, chunkOverlap: Int): TextSplitter =
  TokenTextSplitterImpl(encodingType.encoding, chunkSize, chunkOverlap)

private class TokenTextSplitterImpl(
  private val tokenizer: Encoding,
  private val chunkSize: Int,
  private val chunkOverlap: Int
) : TextSplitter {

  override suspend fun splitText(text: String): List<String> {
    val inputIds = tokenizer.encode(text)
    fun decodeSegment(startIdx: Int): String {
      val end = minOf(startIdx + chunkSize, inputIds.size)
      return tokenizer.decode(inputIds.subList(startIdx, end))
    }

    return inputIds.indices.step(chunkSize - chunkOverlap).map { decodeSegment(it) }
  }

  override suspend fun splitDocuments(documents: List<String>): List<String> =
    documents.flatMap { document -> splitText(document) }

  override suspend fun splitTextInDocuments(text: String): List<String> = splitText(text)
}
