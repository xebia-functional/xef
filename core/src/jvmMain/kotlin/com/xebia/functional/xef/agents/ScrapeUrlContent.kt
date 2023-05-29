package com.xebia.functional.xef.agents

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.loaders.ScrapeURLTextLoader
import com.xebia.functional.xef.textsplitters.TextSplitter
import com.xebia.functional.xef.textsplitters.TokenTextSplitter

suspend fun scrapeUrlContent(
  url: String,
  splitter: TextSplitter =
    TokenTextSplitter(ModelType.GPT_3_5_TURBO, chunkSize = 100, chunkOverlap = 50)
): List<String> = ScrapeURLTextLoader(url).loadAndSplit(splitter)
