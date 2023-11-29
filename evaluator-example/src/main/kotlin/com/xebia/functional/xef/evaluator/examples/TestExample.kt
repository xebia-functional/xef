package com.xebia.functional.xef.evaluator.examples

import arrow.continuations.SuspendApp
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.promptMessage
import com.xebia.functional.xef.evaluator.EitherOps.toJsonFile
import com.xebia.functional.xef.evaluator.models.SuiteSpec

object TestExample {

  @JvmStatic
  fun main(args: Array<String>) = SuspendApp {
    val output: String = args.getOrNull(0) ?: "."

    SuiteSpec(description = "Check GTP3.5 and fake outputs") {
        outputDescription { "Using GPT3.5" }
        outputDescription { "Fake outputs with errors" }

        itemSpec("Please provide a movie title, genre and director") {
          contextDescription { "Contains information about a movie" }
          outputResponse { OpenAI.conversation { promptMessage(input) } }
          outputResponse { "I don't know" }
        }

        itemSpec("Recipe for a chocolate cake") {
          contextDescription { "Contains instructions for making a cake" }
          outputResponse { OpenAI.conversation { promptMessage(input) } }
          outputResponse { "The movie is Jurassic Park" }
        }
      }
      .toJsonFile(output, "data.json")

    println("JSON created successfully")
  }
}
