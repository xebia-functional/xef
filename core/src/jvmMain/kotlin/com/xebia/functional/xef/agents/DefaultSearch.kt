@file:JvmName("Search")
package com.xebia.functional.xef.agents

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.textsplitters.TokenTextSplitter

suspend fun search(prompt: String): List<String> =
  bingSearch(
    search = prompt,
    TokenTextSplitter(ModelType.GPT_3_5_TURBO, chunkSize = 100, chunkOverlap = 50)
  )
