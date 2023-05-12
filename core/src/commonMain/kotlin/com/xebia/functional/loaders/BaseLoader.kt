package com.xebia.functional.loaders

import com.xebia.functional.textsplitters.BaseTextSplitter

interface BaseLoader {
  suspend fun load(): List<String>
  suspend fun loadAndSplit(textSplitter: BaseTextSplitter): List<String>
}
