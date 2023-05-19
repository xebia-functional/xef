package com.xebia.functional.xef.chains

import arrow.core.raise.either
import com.xebia.functional.xef.agents.LLMAgent
import com.xebia.functional.xef.llm.openai.LLMModel
import com.xebia.functional.xef.prompt.Prompt
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec

class LLMAgentSpec :
  StringSpec({
    val model = LLMModel.GPT_3_5_TURBO

    "LLMAgent should return a prediction with a simple template" {
      either {
        val chain = LLMAgent(testLLM, Prompt("Tell me a joke."), model)
        with(chain) { call() }
      } shouldBeRight listOf("I'm not good at jokes")
    }

    "LLMAgent should return a prediction with a more complex template" {
      val template = "My name is foo and I'm 28 years old"
      either {
        val prompt = Prompt(template)
        val chain = LLMAgent(testLLM, prompt, model)
        with(chain) { call() }
      } shouldBeRight listOf("Hello there! Nice to meet you foo")
    }
  })
