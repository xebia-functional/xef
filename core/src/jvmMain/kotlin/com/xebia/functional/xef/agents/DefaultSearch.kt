package com.xebia.functional.xef.agents

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.textsplitters.TokenTextSplitter
import io.github.oshai.KotlinLogging

suspend fun search(prompt: String): List<String> {
  val logger = KotlinLogging.logger {}
  logger.debug { "🔍: searching for `$prompt`" }
  return bingSearch(
    search = prompt,
    TokenTextSplitter(ModelType.GPT_3_5_TURBO, chunkSize = 100, chunkOverlap = 50)
  ).also {
    logger.debug { "🔍: found ${it.size} results" }
  }
}
