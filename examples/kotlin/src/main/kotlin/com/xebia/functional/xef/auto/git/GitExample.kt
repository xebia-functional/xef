package com.xebia.functional.xef.auto.git

import arrow.core.raise.recover
import com.xebia.functional.xef.antlr.languages.Language
import com.xebia.functional.xef.antlr.languages.antlrParser
import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.auto.getOrThrow
import com.xebia.functional.xef.auto.promptMessage
import com.xebia.functional.xef.loaders.FileContent
import com.xebia.functional.xef.loaders.git

suspend fun main() {
  ai {
    contextScope(arrowRepositoryDocuments()) {
      while (true) {
        print("Enter your question: ")
        val line = readlnOrNull() ?: break
        val response = promptMessage(line)
        println(response.firstOrNull())
      }
    }
  }.getOrThrow()
}

private suspend fun arrowRepositoryDocuments() =
  git(
    url = "https://github.com/arrow-kt/arrow",
    fileLoader = ::parseFileWithAntlrIfPossible,
    fileFilter = { it.name.endsWith(".kt") }
  )

private suspend fun parseFileWithAntlrIfPossible(fileContent: FileContent): List<String> {
  val langParser = recover({
    Language.loadByPath(fileContent.path)?.let { language -> antlrParser(language) }
  }) {
    error(it.reason)
  }
  return if (langParser != null) {
    val (parser, context, docs) = langParser(fileContent.content)
    if (docs.isNotEmpty()) println("found ${docs.size} docs in ${context::class.java.name}")
    val foundDocuments = docs.map { doc ->
      doc.toDocument(fileContent.path)
    }
    foundDocuments
  } else listOf(fileContent.toDocument())
}

