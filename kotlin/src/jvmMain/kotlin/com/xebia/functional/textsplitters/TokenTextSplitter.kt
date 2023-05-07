package com.xebia.functional.textsplitters

import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.Encoding
import com.knuddels.jtokkit.api.EncodingRegistry
import com.xebia.functional.Document

class TokenTextSplitterImpl(
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

    override suspend fun splitDocuments(documents: List<Document>): List<Document> {
        return documents.flatMap { document ->
            splitText(document.content).map { content ->
                Document(content)
            }
        }
    }

    override suspend fun splitTextInDocuments(text: String): List<Document> {
        return splitText(text).map { chunk ->
            Document(chunk)
        }
    }
}

val encodingRegistry: EncodingRegistry by lazy {
    Encodings.newDefaultEncodingRegistry()
}

suspend fun TokenTextSplitter(
    encodingName: String = "gpt2",
    modelName: String? = null,
    chunkSize: Int,
    chunkOverlap: Int
): BaseTextSplitter {
    val tokenizer = if (modelName != null) {
        encodingRegistry.getEncodingForModel(modelName).orElseThrow()
    } else {
        encodingRegistry.getEncoding(encodingName).orElseThrow()
    }

    return TokenTextSplitterImpl(tokenizer, chunkSize, chunkOverlap)
}
