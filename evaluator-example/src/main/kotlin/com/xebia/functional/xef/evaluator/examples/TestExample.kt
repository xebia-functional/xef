package com.xebia.functional.xef.evaluator.examples

import ai.xef.openai.StandardModel
import arrow.continuations.SuspendApp
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.evaluator.ItemSpec
import com.xebia.functional.xef.evaluator.SuiteSpec
import com.xebia.functional.xef.evaluator.metrics.AnswerAccuracy
import com.xebia.functional.xef.evaluator.models.ContextDescription
import com.xebia.functional.xef.evaluator.models.OutputDescription
import com.xebia.functional.xef.evaluator.models.OutputResponse
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.user
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object TestExample {

  @JvmStatic
  fun main(args: Array<String>) = SuspendApp {
    val output: String = args.getOrNull(0) ?: "."

    val file = File("$output/data.json")

    val model = StandardModel(CreateChatCompletionRequestModel.gpt_3_5_turbo_16k)

    val spec =
      SuiteSpec(
        description = "Check GTP3.5 and fake outputs",
        model = CreateChatCompletionRequestModel.gpt_4_turbo_preview
      ) {
        +OutputDescription("Using GPT3.5")
        +OutputDescription("Fake outputs with errors")

        +ItemSpec("Please provide a movie title, genre and director") {
          +ContextDescription("Contains information about a movie")

          +OutputResponse { Conversation { promptMessage(Prompt(model) { +user(input) }) } }

          +OutputResponse("I don't know")
        }

        +ItemSpec("Recipe for a chocolate cake") {
          +ContextDescription("Contains instructions for making a cake")

          +OutputResponse { Conversation { promptMessage(Prompt(model) { +user(input) }) } }

          +OutputResponse("The movie is Jurassic Park")
        }
      }

    //    file.writeText(spec.toJSON())
    val res = spec.evaluate<AnswerAccuracy>()
    val prettyRes = Json { prettyPrint = true }.encodeToString(res)
    println(prettyRes)

    println("JSON created successfully")
  }
}
