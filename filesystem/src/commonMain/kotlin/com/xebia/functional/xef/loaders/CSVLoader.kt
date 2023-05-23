package com.xebia.functional.xef.loaders

import com.xebia.functional.xef.csv.RFC4180Parser
import com.xebia.functional.xef.io.DEFAULT
import okio.FileSystem
import okio.Path

/** Creates a CSVLoader based on a Path */
fun CSVLoader(
  filePath: Path,
  hasHeader: Boolean = true,
  rowSeparator: CharSequence = ", ",
  fileSystem: FileSystem = FileSystem.DEFAULT
): BaseLoader = object : BaseLoader {

  override suspend fun load(): List<String> =
    buildList {
      val parser = RFC4180Parser()
      var headers: List<String> = emptyList()
      fileSystem.read(filePath) {
        while (true) {
          val row = readUtf8Line() ?: break
          when {
            hasHeader && this@buildList.size == 0 -> {
              headers = parser.parseLine(row)
            }

            hasHeader -> add(headers.zip(parser.parseLine(row)) { key, value -> "$key: $value" }
              .joinToString(rowSeparator))

            else -> add(parser.parseLine(row).joinToString(rowSeparator))
          }
        }
      }
    }
}
