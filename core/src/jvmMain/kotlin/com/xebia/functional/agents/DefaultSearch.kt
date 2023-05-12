package com.xebia.functional.agents

import com.xebia.functional.textsplitters.TokenTextSplitter

fun search(vararg prompt: String): Collection<ParameterlessAgent<List<String>>> =
  prompt
    .map {
      bingSearch(
        search = it,
        TokenTextSplitter(modelName = "gpt-3.5-turbo", chunkSize = 100, chunkOverlap = 50)
      )
    }
