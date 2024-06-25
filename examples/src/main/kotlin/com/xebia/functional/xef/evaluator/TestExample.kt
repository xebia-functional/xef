package com.xebia.functional.xef.evaluator

import arrow.continuations.SuspendApp
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.evaluator.metrics.AnswerAccuracy
import com.xebia.functional.xef.evaluator.models.OutputDescription
import com.xebia.functional.xef.evaluator.models.OutputResponse
import com.xebia.functional.xef.llm.promptMessage
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.user
import io.github.nomisrev.openapi.CreateChatCompletionRequest

object TestExample {

  @JvmStatic
  fun main(args: Array<String>) = SuspendApp {
    val model = CreateChatCompletionRequest.Model.Gpt35Turbo16k
    val chat = OpenAI(logRequests = true).chat

    val spec =
      SuiteSpec(
        description = "Check GTP3.5 and fake outputs",
        model = CreateChatCompletionRequest.Model.Gpt4TurboPreview
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
    val results = spec.evaluate<AnswerAccuracy>(success = listOf(AnswerAccuracy.yes))
    results.items.forEach {
      println("==============")
      println("  ${it.description}")
      println("==============")
      it.items.zip(it.items.indices).forEach { (item, index) ->
        println()
        println(">> Output ${index + 1}")
        println("Description: ${item.description}")
        println("Success: ${item.success}")
        println()
        println("AI Output:")
        println(item.output)
        println()
      }
      println()
      println()
    }
  }
}
