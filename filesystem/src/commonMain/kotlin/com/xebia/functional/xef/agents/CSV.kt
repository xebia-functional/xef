package com.xebia.functional.xef.agents

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.io.DEFAULT
import com.xebia.functional.xef.loaders.CSVLoader
import com.xebia.functional.xef.textsplitters.BaseTextSplitter
import com.xebia.functional.xef.textsplitters.TokenTextSplitter
import okio.FileSystem
import okio.Path

fun csv(
  vararg files: Path,
  fileSystem: FileSystem = FileSystem.DEFAULT,
  splitter: BaseTextSplitter = TokenTextSplitter(ModelType.GPT_3_5_TURBO, chunkSize = 100, chunkOverlap = 50)
): Collection<ParameterlessAgent<List<String>>> = files.map { file ->
  ParameterlessAgent(name = "Load CSV content", description = "Load CSV content from $file") {
    val loader = CSVLoader(file)
    loader.loadAndSplit(splitter)
  }
}

