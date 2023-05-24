package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.io.DEFAULT
import okio.FileSystem
import okio.Path

/**
 * Creates a Prompt based on a Path
 */
fun Prompt(
  path: Path,
  fileSystem: FileSystem = FileSystem.DEFAULT
): Prompt =
  fileSystem.read(path) {
    Prompt(readUtf8())
  }
