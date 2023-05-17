package com.xebia.functional.xef.loaders

import com.xebia.functional.xef.textsplitters.BaseTextSplitter

interface BaseLoader {
  suspend fun load(): List<String>
  suspend fun loadAndSplit(textSplitter: BaseTextSplitter): List<String> =
    textSplitter.splitDocuments(documents = load())
}
