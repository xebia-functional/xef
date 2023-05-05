package com.xebia.functional.loaders

import com.xebia.functional.Document
import com.xebia.functional.io.DEFAULT
import com.xebia.functional.textsplitters.BaseTextSplitter
import okio.FileSystem
import okio.Path

/** Creates a TextLoader based on a Path */
suspend fun TextLoader(
  filePath: Path,
  fileSystem: FileSystem = FileSystem.DEFAULT
): BaseLoader = object : BaseLoader {

  override suspend fun load(): List<Document> =
    buildList {
      fileSystem.read(filePath) {
        while (true) {
          val line = readUtf8Line() ?: break
          val document = Document(line)
          add(document)
        }
      }
    }

  override suspend fun loadAndSplit(textSplitter: BaseTextSplitter): List<Document> =
    textSplitter.splitDocuments(documents = load())
}
