package com.xebia.functional.xef.antlr.languages

import arrow.core.raise.Raise
import com.xebia.functional.xef.antlr.ANTLRError
import com.xebia.functional.xef.antlr.EntryPoint
import com.xebia.functional.xef.antlr.G4ParserFromURLs

fun Raise<ANTLRError>.kotlin() =
  G4ParserFromURLs(
    EntryPoint(
      method = "kotlinFile",
      lexer = "KotlinLexer",
      parser = "KotlinParser",
      contextDocuments = setOf("KotlinFile", "Declaration"),
    ),
    "https://raw.githubusercontent.com/antlr/grammars-v4/master/kotlin/kotlin/UnicodeClasses.g4",
    "https://raw.githubusercontent.com/antlr/grammars-v4/master/kotlin/kotlin/KotlinLexer.g4",
    "https://raw.githubusercontent.com/antlr/grammars-v4/master/kotlin/kotlin/KotlinParser.g4",
  )
