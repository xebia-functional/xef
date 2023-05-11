package com.xebia.functional.tool

import com.xebia.functional.loaders.ScrapeURLTextLoader
import com.xebia.functional.textsplitters.BaseTextSplitter
import com.xebia.functional.tools.Agent

fun scrapeUrlContent(url: String, splitter: BaseTextSplitter): Agent =
  Agent(name = "Scrape URL content", description = "Scrape the content of $url") {
    val loader = ScrapeURLTextLoader(url)
    loader.loadAndSplit(splitter)
  }
