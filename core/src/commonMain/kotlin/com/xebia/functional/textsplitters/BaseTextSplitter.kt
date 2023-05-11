package com.xebia.functional.textsplitters

interface BaseTextSplitter {
  suspend fun splitText(text: String): List<String>
  suspend fun splitDocuments(documents: List<String>): List<String>
  suspend fun splitTextInDocuments(text: String): List<String>
}
