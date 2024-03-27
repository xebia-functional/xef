package com.xebia.functional.xef.dsl.chat

import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.conversation.MessagePolicy
import com.xebia.functional.xef.conversation.MessagesFromHistory
import com.xebia.functional.xef.conversation.MessagesToHistory
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.assistant
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.system
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.user
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import kotlinx.serialization.Serializable

@Serializable
@Description("A list of books")
data class Books(@Description("The list of books") val books: List<Book>)

@Serializable
@Description("A book")
data class Book(
  @Description("The title of the book") val title: String,
  @Description("The author of the book") val author: String,
  @Description("A 20 word summary of the book") val summary: String
)

suspend fun books(topic: String): Books {
  val model = CreateChatCompletionRequestModel.gpt_3_5_turbo_0125

  val myCustomPrompt =
    Prompt(
      model = model,
      configuration =
        PromptConfiguration {
          temperature = 0.0
          maxTokens = 100
          messagePolicy =
            MessagePolicy(
              historyPercent = 50,
              historyPaddingTokens = 100,
              contextPercent = 50,
              addMessagesFromConversation = MessagesFromHistory.ALL,
              addMessagesToConversation = MessagesToHistory.ALL
            )
        }
    ) {
      +system(
        "You are an assistant in charge of providing a selection of books about topics provided"
      )
      +assistant(
        "I will provide relevant suggestions of books and follow the instructions closely."
      )
      +user("Give me a selection of books about $topic")
    }

  return AI.chat(myCustomPrompt)
}

suspend fun main() {
  val books = books("Mars")
  println(books)
}
