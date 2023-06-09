package com.xebia.functional.xef.antlr

data class EntryPoint(
  val method: String,
  val lexer: String,
  val parser: String,
  val contextDocuments: Set<String> = emptySet(),
)
