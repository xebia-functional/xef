package com.xebia.functional.textsplitters

import arrow.core.flatten
import arrow.core.traverse
import arrow.fx.coroutines.parMap
import arrow.fx.coroutines.parTraverse
import com.xebia.functional.domain.Document

data class SplitterError(val reason: String)

class CharacterTextSplitter(private val separator: String) : BaseTextSplitter {
    override suspend fun splitText(text: String): List<String> =
        text.split(separator)

    override suspend fun splitDocuments(documents: List<Document>): List<Document> =
        documents.flatMap { doc -> doc.content.split(separator) }.map(::Document)

    override suspend fun splitTextInDocuments(text: String): List<Document> =
        text.split(separator).map(::Document)
}
