package com.xebia.functional.loaders

import com.xebia.functional.domain.Document
import com.xebia.functional.textsplitters.BaseTextSplitter
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use

/**
 * Creates a TextLoader based on a Path
 * JVM & Native have overloads for FileSystem.SYSTEM,
 * on NodeJs you need to manually pass FileSystem.SYSTEM.
 *
 * This function can currently not be used on the browser.
 *
 * https://github.com/square/okio/issues/1070
 * https://youtrack.jetbrains.com/issue/KT-47038
 */
suspend fun TextLoader(
    filePath: Path,
    fileSystem: FileSystem
): BaseLoader = object : BaseLoader {

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
