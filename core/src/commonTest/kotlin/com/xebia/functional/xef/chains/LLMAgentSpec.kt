package com.xebia.functional.xef.chains

import arrow.core.raise.either
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.agents.LLMAgent
import com.xebia.functional.xef.llm.openai.LLMModel
import com.xebia.functional.xef.prompt.PromptTemplate
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec

class LLMAgentSpec :
  StringSpec({
    val model = LLMModel.GPT_3_5_TURBO

    "LLMAgent should return a prediction with a simple template" {
      val template = "Tell me {foo}."
      either {
        val promptTemplate = PromptTemplate(template, listOf("foo"))
        val chain = LLMAgent(testLLM, promptTemplate, model)
        with(chain) { call(mapOf("foo" to "a joke")) }
      } shouldBeRight listOf("I'm not good at jokes")
    }

    "LLMAgent should return a prediction with a more complex template" {
      val template = "My name is {name} and I'm {age} years old"
      either {
        val prompt = PromptTemplate(template, listOf("name", "age"))
        val chain = LLMAgent(testLLM, prompt, model)
        with(chain) { call(mapOf("age" to "28", "name" to "foo")) }
      } shouldBeRight listOf("Hello there! Nice to meet you foo")
    }

    "LLMAgent should fail when inputs are not the expected ones from the PromptTemplate" {
      val template = "My name is {name} and I'm {age} years old"
      either {
        val prompt = PromptTemplate(template, listOf("name", "age"))
        val chain = LLMAgent(testLLM, prompt, model)
        with(chain) { call(mapOf("age" to "28", "brand" to "foo")) }
      } shouldBeLeft
        AIError.InvalidInputs(
          "The provided inputs: {age}, {brand} do not match with chain's inputs: {name}, {age}"
        )
    }

    "LLMAgent should fail when the prompt size is larger than the model's max context size of type completion" {
      val template = "Tell me {foo}."
      either {
        val promptTemplate = PromptTemplate(template, listOf("foo"))
        val chain = LLMAgent(maxTokensTestLLM, promptTemplate, testCompletionTinyModel)
        with(chain) { call(mapOf("foo" to "a joke")) }
      } shouldBeLeft AIError.ExceedModelContextLength(1, 5)
    }

    "LLMAgent should fail when the prompt size is larger than the model's max context size of type chat" {
      val template = "Tell me {foo}."
      either {
        val promptTemplate = PromptTemplate(template, listOf("foo"))
        val chain = LLMAgent(maxTokensTestLLM, promptTemplate, testChatTinyModel)
        with(chain) { call(mapOf("foo" to "a joke")) }
      } shouldBeLeft AIError.ExceedModelContextLength(1, 5)
    }

    "LLMAgent should return a limited prediction with length of maxTokens length when passing smaller maxTokens" {
      val template = "Tell me {foo}."
      either {
        val promptTemplate = PromptTemplate(template, listOf("foo"))
        val chain = LLMAgent(maxTokensTestLLM, promptTemplate, model, maxTokens = 10)
        with(chain) { call(mapOf("foo" to "a joke")) }
      } shouldBeRight listOf("No")
    }

    "LLMAgent should return a prediction with max length of contextLength - promptLength when passing big maxTokens" {
      val template = "Tell me {foo}."
      either {
        val promptTemplate = PromptTemplate(template, listOf("foo"))
        val chain = LLMAgent(maxTokensTestLLM, promptTemplate, model, maxTokens = 5000)
        with(chain) { call(mapOf("foo" to "a joke")) }
      } shouldBeRight listOf("I'm not in humor for jokes, buddy")
    }
  })
