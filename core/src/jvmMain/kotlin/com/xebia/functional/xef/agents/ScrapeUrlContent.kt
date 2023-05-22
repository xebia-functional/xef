package com.xebia.functional.xef.agents

import com.xebia.functional.xef.loaders.ScrapeURLTextLoader
import com.xebia.functional.xef.textsplitters.TextSplitter

suspend fun scrapeUrlContent(url: String, splitter: TextSplitter): List<String> =
  ScrapeURLTextLoader(url).loadAndSplit(splitter)
