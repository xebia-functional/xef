package com.xebia.functional.textsplitters

import com.xebia.functional.Document
import com.xebia.functional.tokenizer.Encoding
import com.xebia.functional.tokenizer.ModelType

fun TokenTextSplitter(
  modelType: ModelType,
  chunkSize: Int,
  chunkOverlap: Int
): BaseTextSplitter =
  TokenTextSplitterImpl(modelType.encodingType.encoding, chunkSize, chunkOverlap)

private class TokenTextSplitterImpl(
  private val tokenizer: Encoding,
  private val chunkSize: Int,
  private val chunkOverlap: Int
) : BaseTextSplitter {

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

  override suspend fun splitDocuments(documents: List<Document>): List<Document> =
    documents.flatMap { document ->
      splitText(document.content).map { content -> Document(content) }
    }

  override suspend fun splitTextInDocuments(text: String): List<Document> =
    splitText(text).map { chunk -> Document(chunk) }
}
