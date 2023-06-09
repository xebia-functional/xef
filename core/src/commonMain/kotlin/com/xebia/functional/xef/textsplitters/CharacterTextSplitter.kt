package com.xebia.functional.xef.textsplitters

fun CharacterTextSplitter(separator: String): TextSplitter =
  object : TextSplitter {

    override suspend fun splitText(text: String): List<String> = text.split(separator)

    override suspend fun splitDocuments(documents: List<String>): List<String> =
      documents.flatMap { doc -> doc.split(separator) }

    override suspend fun splitTextInDocuments(text: String): List<String> = text.split(separator)
  }
