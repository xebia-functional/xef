package com.xebia.functional.xef.conversation.fields

import ai.xef.openai.StandardModel
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.prompt.Prompt
import kotlinx.serialization.Serializable

@Serializable
data class Book(
  @Description("The title of the book.") val title: String,
  @Description("The author of the book.") val author: String,
  @Description("A 50 word summary of the book.") val summary: String
)

suspend fun main() {
  Conversation {
    val model = StandardModel(CreateChatCompletionRequestModel.gpt_3_5_turbo_16k_0613)
    val toKillAMockingbird: Book = prompt(Prompt(model, "To Kill a Mockingbird by Harper Lee"))
    println(toKillAMockingbird)
  }
}
