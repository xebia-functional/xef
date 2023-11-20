package com.xebia.funcional.xef.evaluator.examples

import com.xebia.funcional.xef.evaluator.TestSpecItem
import com.xebia.funcional.xef.evaluator.TestsSpec
import com.xebia.funcional.xef.evaluator.models.ContextDescription
import com.xebia.funcional.xef.evaluator.models.OutputDescription
import com.xebia.funcional.xef.evaluator.models.OutputResponse
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.promptMessage

suspend fun main() {

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

  println(spec.toJSON())
}
