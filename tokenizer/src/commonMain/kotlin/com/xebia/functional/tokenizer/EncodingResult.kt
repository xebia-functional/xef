package com.xebia.functional.tokenizer

/** The result of encoding operation. */
data class EncodingResult(
  val tokens: List<Int>,
  /** if the token list was truncated because the maximum token length was exceeded */
  val isTruncated: Boolean
)
