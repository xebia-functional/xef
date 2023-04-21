package com.xebia.functional.textsplitters

import com.xebia.functional.domain.Document

interface BaseTextSplitter {
    suspend fun splitText(text: String): List<String>
    suspend fun splitDocuments(documents: List<Document>): List<Document>
    suspend fun splitTextInDocuments(text: String): List<Document>
}