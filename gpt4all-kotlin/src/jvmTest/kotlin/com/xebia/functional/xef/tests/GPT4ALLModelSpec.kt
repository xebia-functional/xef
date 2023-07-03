package com.xebia.functional.xef.tests

import com.xebia.functional.gpt4all.Gpt4AllModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan

class GPT4ALLModelSpec :
  StringSpec({
    "should return a list of supported models by GPT4ALL" {
      Gpt4AllModel.supportedModels().size shouldBeGreaterThan 0
    }
  })
