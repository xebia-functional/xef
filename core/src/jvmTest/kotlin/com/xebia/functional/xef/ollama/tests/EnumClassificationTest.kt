package com.xebia.functional.xef.ollama.tests

import com.xebia.functional.xef.ollama.tests.models.OllamaModels
import com.xebia.functional.xef.ollama.tests.models.Sentiment
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class EnumClassificationTest : OllamaTests() {

  @Test
  fun `positive sentiment`() {
    runBlocking {
      val sentiment =
        ollama<Sentiment>(
          model = OllamaModels.Gemma2B,
          prompt = "The context of the situation is very positive.",
        )
      assert(sentiment == Sentiment.POSITIVE) { "Expected POSITIVE but got $sentiment" }
    }
  }

  @Test
  fun `negative sentiment`() {
    runBlocking {
      val sentiment =
        ollama<Sentiment>(
          model = OllamaModels.LLama3_8B,
          prompt = "The context of the situation is very negative.",
        )
      assert(sentiment == Sentiment.NEGATIVE) { "Expected NEGATIVE but got $sentiment" }
    }
  }
}
