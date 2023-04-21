package com.xebia.functional.loaders

import com.xebia.functional.domain.Document
import com.xebia.functional.textsplitters.BaseTextSplitter
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use

data class InvalidText(val reason: String)

class TextLoader(private val filePath: Path, private val fileSystem: FileSystem) : BaseLoader {
    override suspend fun load(): List<Document> =
        buildList {
            fileSystem.source(filePath).buffer().use { source ->
                while (true) {
                    val line = source.readUtf8Line() ?: break
                    val document = Document(line)
                    add(document)
                }
            }
        }

    override suspend fun loadAndSplit(textSplitter: BaseTextSplitter): List<Document> =
        textSplitter.splitDocuments(documents = load())
}
