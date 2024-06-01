package com.xebia.functional.xef.evaluator

import ai.xef.OpenAI
import arrow.continuations.SuspendApp
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.evaluator.metrics.AnswerAccuracy
import com.xebia.functional.xef.evaluator.models.ModelsPricing
import com.xebia.functional.xef.evaluator.models.OutputDescription
import com.xebia.functional.xef.evaluator.models.OutputResponse
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.user
import java.io.File

object TestExample {

  @JvmStatic
  fun main(args: Array<String>) = SuspendApp {
    val chat = OpenAI.Chat.gpt4o()

    val spec =
      SuiteSpec(
        description = "Check GTP3.5 and fake outputs",
        model = chat
      ) {
        val gpt35Description = OutputDescription("Using GPT3.5")
        val fakeOutputs = OutputDescription("Fake outputs with errors")

        +ItemSpec(
          input = "Please provide a movie title, genre and director",
          context = "Contains information about a movie"
        ) {
          +OutputResponse(gpt35Description, ModelsPricing.gpt4o) {
            Conversation { chat(Prompt { +user(input) }) }
          }

          +OutputResponse(description = fakeOutputs, null, value = "I don't know")
        }

        +ItemSpec(
          input = "Recipe for a chocolate cake",
          context = "Contains instructions for making a cake"
        ) {
          +OutputResponse(gpt35Description, ModelsPricing.gpt4) {
            Conversation { chat(Prompt { +user(input) }) }
          }

          +OutputResponse(description = fakeOutputs, null, value = "The movie is Jurassic Park")
        }
      }
    val results = spec.evaluate<AnswerAccuracy>(success = listOf(AnswerAccuracy.yes))

    val outputPath = System.getProperty("user.dir") + "/build/testSuite"
    File(outputPath).mkdir()
    val fileHtml = File("$outputPath/test.html")
    fileHtml.writeText(SuiteSpec.toHtml(results, "test.html").value)

    val fileMarkDown = File("$outputPath/test.md")
    fileMarkDown.writeText(SuiteSpec.toMarkdown(results, "test.md").value)

    results.items.forEach {
      println("==============")
      println("  ${it.description}")
      println("==============")
      it.items.zip(it.items.indices).forEach { (item, index) ->
        println()
        println(">> Output ${index + 1}")
        println("Description: ${item.description}")
        println("Usage: ${item.usage}")
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
