package com.xebia.functional.xef.evaluator

import arrow.continuations.SuspendApp
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.evaluator.metrics.AnswerAccuracy
import com.xebia.functional.xef.evaluator.models.OutputDescription
import com.xebia.functional.xef.evaluator.models.OutputResponse
import com.xebia.functional.xef.llm.promptMessage
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.user

object TestExample {

  @JvmStatic
  fun main(args: Array<String>) = SuspendApp {
    val model = CreateChatCompletionRequestModel._3_5_turbo_16k
    val chat = OpenAI().chat

    val spec =
      SuiteSpec(
        description = "Check GTP3.5 and fake outputs",
        model = CreateChatCompletionRequestModel._4_turbo_preview
      ) {
        val gpt35Description = OutputDescription("Using GPT3.5")
        val fakeOutputs = OutputDescription("Fake outputs with errors")

        +ItemSpec(
          input = "Please provide a movie title, genre and director",
          context = "Contains information about a movie"
        ) {
          +OutputResponse(gpt35Description) {
            Conversation { chat.promptMessage(Prompt(model) { +user(input) }) }
          }

          +OutputResponse(description = fakeOutputs, value = "I don't know")
        }

        +ItemSpec(
          input = "Recipe for a chocolate cake",
          context = "Contains instructions for making a cake"
        ) {
          +OutputResponse(gpt35Description) {
            Conversation { chat.promptMessage(Prompt(model) { +user(input) }) }
          }

          +OutputResponse(description = fakeOutputs, value = "The movie is Jurassic Park")
        }
      }
    spec.evaluate<AnswerAccuracy>()
  }
}
