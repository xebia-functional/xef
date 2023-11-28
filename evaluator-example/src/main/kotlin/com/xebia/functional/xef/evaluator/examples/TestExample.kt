package com.xebia.funcional.xef.evaluator.examples

import arrow.continuations.SuspendApp
import arrow.core.Either
import arrow.core.NonEmptyList
import com.xebia.funcional.xef.evaluator.models.SuiteSpec
import com.xebia.funcional.xef.evaluator.models.errors.ValidationError
import com.xebia.funcional.xef.evaluator.models.toJsonFile
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.promptMessage

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
    }.toJsonFile(output, "data.json")

    println("JSON created successfully")
  }
}
