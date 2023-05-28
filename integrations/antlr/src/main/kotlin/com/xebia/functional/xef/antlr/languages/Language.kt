package com.xebia.functional.xef.antlr.languages

import arrow.core.raise.Raise
import arrow.fx.coroutines.Atomic
import com.xebia.functional.xef.antlr.ANTLRError
import com.xebia.functional.xef.antlr.ParserResult
import okio.Path

enum class Language {
  KOTLIN,
  ;

  companion object {
    fun loadByPath(path: Path): Language? = when  {
      path.toString().endsWith(".kt") -> KOTLIN
      else -> null
    }
  }
}

private val loadedParsers: Atomic<Map<Language, (String) -> ParserResult>> = Atomic.unsafe(emptyMap())

suspend fun Raise<ANTLRError>.antlrParser(language: Language): (String) -> ParserResult {
  val parsers = loadedParsers.get()
  val parser = parsers[language]
  return if (parser != null) parser
  else {
    val newParser = when (language) {
      Language.KOTLIN -> kotlin()
    }
    loadedParsers.update { it + (language to newParser) }
    newParser
  }
}
