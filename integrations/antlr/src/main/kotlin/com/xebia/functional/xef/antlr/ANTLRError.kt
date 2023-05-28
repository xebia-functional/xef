package com.xebia.functional.xef.antlr

sealed class ANTLRError(open val reason: String) {
  data class LexerError(override val reason: String) : ANTLRError(reason)
  data class ParserError(override val reason: String) : ANTLRError(reason)
}
