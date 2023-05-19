package com.xebia.functional.xef.agents

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.textsplitters.TokenTextSplitter

fun search(vararg prompt: String): Collection<ParameterlessAgent<List<String>>> =
  prompt.map {
    bingSearch(
      search = it,
      TokenTextSplitter(ModelType.GPT_3_5_TURBO, chunkSize = 100, chunkOverlap = 50)
    )
  }
