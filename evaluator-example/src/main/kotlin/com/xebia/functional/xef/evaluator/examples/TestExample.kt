package com.xebia.functional.xef.evaluator.examples

import arrow.continuations.SuspendApp
import com.xebia.functional.xef.evaluator.TestSpecItem
import com.xebia.functional.xef.evaluator.TestsSpec
import com.xebia.functional.xef.evaluator.models.ContextDescription
import com.xebia.functional.xef.evaluator.models.OutputDescription
import com.xebia.functional.xef.evaluator.models.OutputResponse
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.promptMessage
import java.io.File

object TestExample {

  @JvmStatic
  fun main(args: Array<String>) = SuspendApp {
    val output: String = args.getOrNull(0) ?: "."

    val file = File("$output/data.json")

    val spec =
      TestsSpec(description = "Check GTP3.5 and fake outputs") {
        +OutputDescription("Using GPT3.5")
        +OutputDescription("Fake outputs with errors")

        +TestSpecItem("Please provide a movie title, genre and director") {
          +ContextDescription("Contains information about a movie")

          +OutputResponse { OpenAI.conversation { promptMessage(input) } }

          +OutputResponse("I don't know")
        }

        +TestSpecItem("Recipe for a chocolate cake") {
          +ContextDescription("Contains instructions for making a cake")

          +OutputResponse { OpenAI.conversation { promptMessage(input) } }

          +OutputResponse("The movie is Jurassic Park")
        }
      }

    file.writeText(spec.toJSON())

    println("JSON created successfully")
  }
}
