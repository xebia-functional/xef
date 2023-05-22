package com.xebia.functional.xef.agents

import com.xebia.functional.xef.auto.AIScope
import com.xebia.functional.xef.textsplitters.TokenTextSplitter

suspend fun AIScope.search(prompt: String): List<String> =
  bingSearch(search = prompt, TokenTextSplitter(chunkSize = 100, chunkOverlap = 50))
