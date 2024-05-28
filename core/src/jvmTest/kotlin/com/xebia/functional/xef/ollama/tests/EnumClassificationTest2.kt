package com.xebia.functional.xef.ollama.tests

import com.xebia.functional.xef.ollama.tests.EnumClassificationTest.Companion.expectSentiment
import com.xebia.functional.xef.ollama.tests.models.OllamaModels
import com.xebia.functional.xef.ollama.tests.models.Sentiment
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class EnumClassificationTest2 : OllamaTests() {
  @Test
  fun `enum classification 2`() {
    runBlocking {
      val models = setOf(OllamaModels.LLama3_8B)
      val sentiments =
        ollama<Sentiment>(
          models = models,
          prompt = "The sentiment of this text is negative.",
        )
      expectSentiment(Sentiment.NEGATIVE, sentiments, models)
    }
  }
}
