package com.xebia.functional.xef.evaluator.examples

import ai.xef.openai.StandardModel
import arrow.continuations.SuspendApp
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.evaluator.EitherOps.toJsonFile
import com.xebia.functional.xef.evaluator.models.SuiteSpec
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.user

object TestExample {

  @JvmStatic
  fun main(args: Array<String>) = SuspendApp {
    val output: String = args.getOrNull(0) ?: "."

    val model = StandardModel(CreateChatCompletionRequestModel.gpt_3_5_turbo_16k)

    SuiteSpec(description = "Check GTP3.5 and fake outputs") {
        outputDescription { "Using GPT3.5" }
        outputDescription { "Fake outputs with errors" }

        itemSpec("Please provide a movie title, genre and director") {
          contextDescription { "Contains information about a movie" }
          outputResponse { Conversation { promptMessage(Prompt(model) { +user(input) }) } }
          outputResponse { "I don't know" }
        }

        itemSpec("Recipe for a chocolate cake") {
          contextDescription { "Contains instructions for making a cake" }
          outputResponse { Conversation { promptMessage(Prompt(model) { +user(input) }) } }
          outputResponse { "The movie is Jurassic Park" }
        }
      }
      .toJsonFile(output, "data.json")

    println("JSON created successfully")
  }
}
