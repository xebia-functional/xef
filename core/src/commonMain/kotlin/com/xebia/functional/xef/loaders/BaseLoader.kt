package com.xebia.functional.xef.loaders

import com.xebia.functional.xef.textsplitters.TextSplitter

interface BaseLoader {
  suspend fun load(): List<String>

  suspend fun loadAndSplit(textSplitter: TextSplitter): List<String> =
    textSplitter.splitDocuments(documents = load())
}
