package com.xebia.functional.textsplitters

import com.xebia.functional.Document

suspend fun CharacterTextSplitter(
    separator: String
): BaseTextSplitter = object : BaseTextSplitter {

    override suspend fun splitText(text: String): List<String> =
        text.split(separator)

    override suspend fun splitDocuments(documents: List<Document>): List<Document> =
        documents.flatMap { doc -> doc.content.split(separator) }.map(::Document)

    override suspend fun splitTextInDocuments(text: String): List<Document> =
        text.split(separator).map(::Document)
}
