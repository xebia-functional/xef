package com.xebia.functional.xef.loaders

import com.xebia.functional.xef.io.DEFAULT
import okio.FileSystem
import okio.Path

/** Creates a CSVLoader based on a Path */
fun CSVLoader(
  filePath: Path,
  fileSystem: FileSystem = FileSystem.DEFAULT
): BaseLoader = object : BaseLoader {

  override suspend fun load(): List<String> =
    buildList {
      var headerLine : String = ""
      fileSystem.read(filePath) {
        while (true) {
          val line = readUtf8Line() ?: break
          if (this@buildList.size == 0)
            headerLine = line
          val headers = headerLine.split(",")
          val values = line.split(",")
          val map = headers.zip(values).toMap()
          add(recordMapToDocument(map))
        }
      }
    }

  private fun recordMapToDocument(map: Map<String, String>): String {
    return map.entries.joinToString(separator = "\n") { (key, value) ->
      "$key: $value"
    }
  }

}
