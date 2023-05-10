package com.xebia.functional.tokenizer

actual val regex: Regex
  get() = Regex("""('s|'t|'re|'ve|'m|'ll|'d)|[^\r\n\p{L}\p{N}]?\p{L}+|\p{N}{1,3}| ?[^\s\p{L}\p{N}]+[\r\n]*|\s*[\r\n]+|\s+(?!\S)|\s+""", RegexOption.IGNORE_CASE)
