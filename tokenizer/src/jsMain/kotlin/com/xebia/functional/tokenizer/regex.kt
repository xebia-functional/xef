package com.xebia.functional.tokenizer

actual val p50k_regex: Regex
  get() = Regex("""'s|'t|'re|'ve|'m|'ll|'d| ?\p{L}+| ?\p{N}+| ?[^\s\p{L}\p{N}]+|\s+(?!\S)|\s+""")

actual val cl100k_base_regex: Regex
  get() =
    Regex(
      """('s|'t|'re|'ve|'m|'ll|'d)|[^\r\n\p{L}\p{N}]?\p{L}+|\p{N}{1,3}| ?[^\s\p{L}\p{N}]+[\r\n]*|\s*[\r\n]+|\s+(?!\S)|\s+""",
      RegexOption.IGNORE_CASE,
    )

actual val o200k_base_regex: Regex
  get() =
    Regex(
      listOf(
          """[^\r\n\p{L}\p{N}]?[\p{Lu}\p{Lt}\p{Lm}\p{Lo}\p{M}]*[\p{Ll}\p{Lm}\p{Lo}\p{M}]+('s|'t|'re|'ve|'m|'ll|'d)?""",
          """[^\r\n\p{L}\p{N}]?[\p{Lu}\p{Lt}\p{Lm}\p{Lo}\p{M}]+[\p{Ll}\p{Lm}\p{Lo}\p{M}]*('s|'t|'re|'ve|'m|'ll|'d)?""",
          """\p{N}{1,3}""",
          """?[^\s\p{L}\p{N}]+[\r\n/]*""",
          """\s*[\r\n]+""",
          """\s+(?!\S)""",
          """\s+""",
        )
        .joinToString("|"),
      RegexOption.IGNORE_CASE,
    )
