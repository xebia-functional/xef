package com.xebia.functional.xef.agents

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.io.DEFAULT
import com.xebia.functional.xef.loaders.CSVLoader
import com.xebia.functional.xef.textsplitters.TextSplitter
import com.xebia.functional.xef.textsplitters.TokenTextSplitter
import okio.FileSystem
import okio.Path

suspend fun csv(
  vararg files: Path,
  fileSystem: FileSystem = FileSystem.DEFAULT,
  splitter: TextSplitter = TokenTextSplitter(ModelType.GPT_3_5_TURBO, chunkSize = 100, chunkOverlap = 50)
): List<String> = files.flatMap { file ->
    val loader = CSVLoader(file, fileSystem)
    loader.loadAndSplit(splitter)
}

