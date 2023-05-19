package com.xebia.functional.xef.agents

import com.xebia.functional.xef.textsplitters.TokenTextSplitter

suspend fun search(prompt: String): List<String> =
  bingSearch(
    search = prompt,
    TokenTextSplitter(modelName = "gpt-3.5-turbo", chunkSize = 100, chunkOverlap = 50)
  )
