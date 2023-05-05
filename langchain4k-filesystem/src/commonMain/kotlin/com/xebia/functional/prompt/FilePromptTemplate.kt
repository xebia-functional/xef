package com.xebia.functional.prompt

import arrow.core.raise.Raise
import com.xebia.functional.io.DEFAULT
import okio.FileSystem
import okio.Path

/**
 * Creates a PromptTemplate based on a Path
 */
suspend fun Raise<InvalidTemplate>.PromptTemplate(
  path: Path,
  variables: List<String>,
  fileSystem: FileSystem = FileSystem.DEFAULT
): PromptTemplate =
  fileSystem.read(path) {
    val template = readUtf8()
    val config = Config(template, variables)
    PromptTemplate(config)
  }