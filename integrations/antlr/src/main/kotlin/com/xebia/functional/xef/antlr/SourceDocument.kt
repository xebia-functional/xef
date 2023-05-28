package com.xebia.functional.xef.antlr

import okio.Path

data class SourceDocument(
  val tempFolder: Path,
  val label : String,
  val startOffset: Int,
  val endOffset: Int,
  val source: String,
) {
  fun toDocument(file: Path): String =
    """
      |---
      |file: ${file.relativeTo(tempFolder)}
      |label: $label
      |startOffset: $startOffset
      |endOffset: $endOffset
      |---
      |
      |$source
      |
      |""".trimMargin()
}
