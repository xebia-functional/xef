package com.xebia.functional.xef.errors

import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.user
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain

class SerializationErrorTests :
  StringSpec({
    val config =
      Config(
        token = "<bad-token>",
      )
    val openAI = OpenAI(config)
    val chat = openAI.chat
    val model = CreateChatCompletionRequestModel.gpt_3_5_turbo

    "serialization errors should include response" {
      try {
        val prompt =
          Prompt(
            model = model,
            configuration = PromptConfiguration.invoke { maxDeserializationAttempts = 1 }
          ) {
            +user("Hello, how are you?")
          }
        AI<String>(prompt = prompt, api = chat)
      } catch (e: Exception) {
        e.message.shouldContain("Incorrect API key")
      }
    }
  })
