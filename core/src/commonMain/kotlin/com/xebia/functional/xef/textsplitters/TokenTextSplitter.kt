package com.xebia.functional.xef.textsplitters

import com.xebia.functional.tokenizer.Encoding
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.auto.AIScope

fun TokenTextSplitter(model: ModelType, chunkSize: Int, chunkOverlap: Int): TextSplitter =
  TokenTextSplitterImpl(model.encoding, chunkSize, chunkOverlap)

fun AIScope.TokenTextSplitter(chunkSize: Int, chunkOverlap: Int): TextSplitter =
  TokenTextSplitter(model, chunkSize, chunkOverlap)

private class TokenTextSplitterImpl(
  private val tokenizer: Encoding,
  private val chunkSize: Int,
  private val chunkOverlap: Int
) : TextSplitter {

  override suspend fun splitText(text: String): List<String> {
    val inputIds = tokenizer.encode(text)
    val stepSize = chunkSize - chunkOverlap

    return inputIds.indices
      .asSequence()
      .filter { it % stepSize == 0 }
      .map { startIdx -> inputIds.subList(startIdx, minOf(startIdx + chunkSize, inputIds.size)) }
      .map { chunkIds -> tokenizer.decode(chunkIds) }
      .toList()
  }

  override suspend fun splitDocuments(documents: List<String>): List<String> =
    documents.flatMap { document -> splitText(document) }

  override suspend fun splitTextInDocuments(text: String): List<String> = splitText(text)
}
