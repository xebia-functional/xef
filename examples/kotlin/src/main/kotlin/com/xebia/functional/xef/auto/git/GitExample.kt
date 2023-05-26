package com.xebia.functional.xef.auto.git

import com.xebia.functional.xef.agents.scrapeUrlContent
import com.xebia.functional.xef.agents.search
import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.auto.getOrThrow
import com.xebia.functional.xef.auto.promptMessage
import com.xebia.functional.xef.loaders.git
import okio.Path

suspend fun main() = ai {
  contextScope(
    scrapeUrlContent("https://arrow-kt.io") +
    git("https://github.com/arrow-kt/arrow", fileFilter = ::interestingFiles) +
      git("https://github.com/arrow-kt/arrow-website", fileFilter = ::interestingFiles),
  ) {
    while (true) {
      print("Enter your question: ")
      val line = readlnOrNull() ?: break
      val response = promptMessage(line)
      println(response.firstOrNull())
    }
  }
}.getOrThrow()

private fun interestingFiles(path: Path) =
  path.name.endsWith(".kt") ||
    path.name.endsWith(".md", ignoreCase = true)
