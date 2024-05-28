package com.xebia.functional.xef.ollama.tests

import com.xebia.functional.xef.ollama.tests.models.OllamaModels
import com.xebia.functional.xef.ollama.tests.models.Sentiment
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class EnumClassificationTest : OllamaTests() {
  @Test
  fun `enum classification`() {
    runBlocking {
      val models = setOf(OllamaModels.Gemma2B)
      val sentiments =
        ollama<Sentiment>(
          models = models,
          prompt = "The sentiment of this text is positive.",
        )
      expectSentiment(Sentiment.POSITIVE, sentiments, models)
    }
  }

  private fun expectSentiment(
    expected: Sentiment,
    sentiments: List<Sentiment>,
    models: Set<String>
  ) {
    assert(sentiments.size == models.size) {
      "Expected ${models.size} results but got ${sentiments.size}"
    }
    sentiments.forEach { sentiment ->
      assert(sentiment == expected) { "Expected $expected but got $sentiment" }
    }
  }
}
