package com.xebia.functional.tool

import com.xebia.functional.textsplitters.TokenTextSplitter
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.tools.Agent

suspend fun search(vararg prompt: String): Array<out Agent> =
  prompt
    .map {
      bingSearch(
        search = it,
        TokenTextSplitter(ModelType.GPT_3_5_TURBO, chunkSize = 100, chunkOverlap = 50)
      )
    }.toTypedArray()
