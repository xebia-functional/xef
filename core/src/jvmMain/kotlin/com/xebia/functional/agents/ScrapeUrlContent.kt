package com.xebia.functional.agents

import com.xebia.functional.loaders.ScrapeURLTextLoader
import com.xebia.functional.textsplitters.BaseTextSplitter

fun scrapeUrlContent(url: String, splitter: BaseTextSplitter): ParameterlessAgent<List<String>> =
  ParameterlessAgent(name = "Scrape URL content", description = "Scrape the content of $url") {
    val loader = ScrapeURLTextLoader(url)
    loader.loadAndSplit(splitter)
  }
