package com.xebia.functional.xef.agents

import com.xebia.functional.xef.loaders.ScrapeURLTextLoader
import com.xebia.functional.xef.textsplitters.BaseTextSplitter

fun scrapeUrlContent(url: String, splitter: BaseTextSplitter): ParameterlessAgent<List<String>> =
  ParameterlessAgent(name = "Scrape URL content", description = "Scrape the content of $url") {
    val loader = ScrapeURLTextLoader(url)
    loader.loadAndSplit(splitter)
  }
