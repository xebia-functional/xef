package com.xebia.functional.xef.conversation.conversations

import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.MessagesFromHistory
import com.xebia.functional.xef.conversation.MessagesToHistory
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.system
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.user
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import kotlinx.serialization.Serializable

@Serializable data class Animal(val name: String, val habitat: String, val diet: String)

@Serializable
data class Invention(val name: String, val inventor: String, val year: Int, val purpose: String)

suspend fun main() {
  // This example contemplate the case of using OpenTelemetry for metrics
  // To run the example with OpenTelemetry, you can execute the following commands:
  //  - # cd server/docker/opentelemetry
  //  - # docker-compose up

  Conversation(
    //    metric = com.xebia.functional.xef.opentelemetry.OpenTelemetryMetric(),
    metric = com.xebia.functional.xef.metrics.LogsMetric()
  ) {
    metric.customSpan("Animal Example") {
      val configNoneFromConversation = PromptConfiguration {
        messagePolicy { addMessagesFromConversation = MessagesFromHistory.NONE }
      }
      val model = CreateChatCompletionRequestModel._3_5_turbo_16k_0613
      val animal: Animal =
        AI(
          Prompt(model) { +user("A unique animal species.") }
            .copy(configuration = configNoneFromConversation)
        )

      val invention: Invention =
        AI(
          Prompt(model) { +user("A groundbreaking invention from the 20th century.") }
            .copy(configuration = configNoneFromConversation)
        )

      println("\nAnimal: $animal")
      println("Invention: $invention")

      val storyPrompt =
        Prompt(model) {
            +system("You are a writer for a science fiction magazine.")
            +user("Write a short story of 200 words that involves the animal and the invention")
          }
          .copy(
            configuration =
              PromptConfiguration {
                messagePolicy { addMessagesToConversation = MessagesToHistory.ONLY_SYSTEM_MESSAGES }
              }
          )

      val story: String = AI(storyPrompt)

      println("\nStory 1:\n$story\n")

      val storyPrompt2 =
        Prompt(model) {
          +user("Write a short story of 100 words that involves the animal in a city called Cadiz")
        }

      val story2: String = AI(storyPrompt2)

      println("\nStory 2:\n$story2\n")
    }
  }
}
