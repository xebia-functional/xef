package com.xebia.functional.xef.agents

import com.xebia.functional.xef.loaders.ScrapeURLTextLoader
import com.xebia.functional.xef.textsplitters.BaseTextSplitter

suspend fun scrapeUrlContent(url: String, splitter: BaseTextSplitter): List<String> =
  ScrapeURLTextLoader(url).loadAndSplit(splitter)
