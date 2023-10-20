package com.xebia.functional.xef.conversation.conversations

import com.xebia.functional.xef.conversation.MessagesFromHistory
import com.xebia.functional.xef.conversation.MessagesToHistory
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.prompt
import com.xebia.functional.xef.conversation.llm.openai.promptMessage
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import com.xebia.functional.xef.prompt.templates.user
import kotlinx.serialization.Serializable

@Serializable data class Animal(val name: String, val habitat: String, val diet: String)

@Serializable
data class Invention(val name: String, val inventor: String, val year: Int, val purpose: String)

suspend fun main() {
  // This example contemplate the case of using OpenTelemetry for metrics
  // To run the example with OpenTelemetry, you can execute the following commands:
  //  - # docker compose-up server/docker/opentelemetry

  // OpenAI.conversation(LocalVectorStore(OpenAI().DEFAULT_EMBEDDING), OpenTelemetryMetric())

  val configNothingFromConversation = PromptConfiguration {
    temperature = 0.0
    messagePolicy { addMessagesFromConversation = MessagesFromHistory.NONE }
  }

  val configNothingToConversation = PromptConfiguration {
    temperature = 0.0
    messagePolicy { addMessagesToConversation = MessagesToHistory.NONE }
  }

  OpenAI.conversation(system = "You are a writer for a science fiction magazine.") {
    val animal: Animal =
      prompt<Animal>(
        Prompt { +user("A unique animal species.") }
          .copy(configuration = configNothingFromConversation)
      )

    val invention: Invention =
      prompt(
        Prompt { +user("A groundbreaking invention from the 20th century.") }
          .copy(configuration = configNothingFromConversation)
      )

    println()
    println("Animal: $animal")
    println("Invention: $invention")
    println()

    val storyPrompt =
      Prompt {
          +user("Write a short story of 200 words that involves the animal and the invention")
        }
        .copy(configuration = configNothingToConversation)

    val story: String = promptMessage(storyPrompt)

    println()
    println("Story 1:")
    println()
    println(story)
    println()
    println()

    val storyPrompt2 =
      Prompt {
          +user("Write a short story of 100 words that involves the animal in a city called Cadiz")
        }
        .copy(configuration = configNothingToConversation)

    val story2: String = promptMessage(storyPrompt2)

    println()
    println("Story 2:")
    println()
    println(story2)
  }
}
