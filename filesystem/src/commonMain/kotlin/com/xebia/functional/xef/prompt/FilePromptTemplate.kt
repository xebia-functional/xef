package com.xebia.functional.xef.prompt

import arrow.core.raise.Raise
import com.xebia.functional.xef.io.DEFAULT
import okio.FileSystem
import okio.Path

/**
 * Creates a PromptTemplate based on a Path
 */
fun Raise<InvalidTemplate>.PromptTemplate(
  path: Path,
  fileSystem: FileSystem = FileSystem.DEFAULT
): PromptTemplate =
  fileSystem.read(path) {
    PromptTemplate.either(readUtf8()).bind()
  }
