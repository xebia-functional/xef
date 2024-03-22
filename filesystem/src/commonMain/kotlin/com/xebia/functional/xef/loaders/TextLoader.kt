package com.xebia.functional.xef.loaders

import com.xebia.functional.xef.io.DEFAULT
import okio.FileSystem
import okio.Path

/** Creates a TextLoader based on a Path */
suspend fun TextLoader(
  filePath: Path,
  fileSystem: FileSystem = FileSystem.DEFAULT
): BaseLoader = object : BaseLoader {

  override suspend fun load(): List<String> =
    buildList {
      fileSystem.read(filePath) {
        while (true) {
          val line = readUtf8Line() ?: break
          add(line)
        }
      }
    }

}
