package com.xebia.functional.xef.conversation.conversations

import com.xebia.functional.xef.conversation.MessagesFromHistory
import com.xebia.functional.xef.conversation.MessagesToHistory
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.prompt
import com.xebia.functional.xef.conversation.llm.openai.promptMessage
import com.xebia.functional.xef.metrics.LogsMetric
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import kotlinx.serialization.Serializable

@Serializable data class Animal(val name: String, val habitat: String, val diet: String)

@Serializable
data class Invention(val name: String, val inventor: String, val year: Int, val purpose: String)

suspend fun main() {
  // This example contemplate the case of using OpenTelemetry for metrics
  // To run the example with OpenTelemetry, you can execute the following commands:
  //  - # docker compose-up server/docker/opentelemetry

  //   OpenAI.conversation(
  //     com.xebia.functional.xef.store.LocalVectorStore(OpenAI().DEFAULT_EMBEDDING),
  //     com.xebia.functional.xef.opentelemetry.OpenTelemetryMetric()) {

  val configNoneFromConversation = PromptConfiguration {
    messagePolicy { addMessagesFromConversation = MessagesFromHistory.NONE }
  }

  OpenAI.conversation(metric = LogsMetric()) {
    val animal: Animal =
      prompt<Animal>(
        Prompt { +user("A unique animal species.") }
          .copy(configuration = configNoneFromConversation)
      )

    val invention: Invention =
      prompt(
        Prompt { +user("A groundbreaking invention from the 20th century.") }
          .copy(configuration = configNoneFromConversation)
      )

    println("\nAnimal: $animal")
    println("Invention: $invention")

    val storyPrompt =
      Prompt {
          +system("You are a writer for a science fiction magazine.")
          +user("Write a short story of 200 words that involves the animal and the invention")
        }
        .copy(
          configuration =
            PromptConfiguration {
              messagePolicy { addMessagesToConversation = MessagesToHistory.ONLY_SYSTEM_MESSAGES }
            }
        )

    val story: String = promptMessage(storyPrompt)

    println("\nStory 1:\n$story\n")

    val storyPrompt2 = Prompt {
      +user("Write a short story of 100 words that involves the animal in a city called Cadiz")
    }

    val story2: String = promptMessage(storyPrompt2)

    println("\nStory 2:\n$story2\n")
  }
}
