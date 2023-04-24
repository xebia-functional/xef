package com.xebia.functional.loaders

import okio.FileSystem
import okio.Path

suspend fun TextLoader(
  filePath: Path
): BaseLoader =
  TextLoader(filePath, FileSystem.SYSTEM)
