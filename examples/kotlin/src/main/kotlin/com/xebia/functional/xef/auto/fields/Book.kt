package com.xebia.functional.xef.auto.fields

import com.xebia.functional.xef.auto.Description
import com.xebia.functional.xef.auto.conversation
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable
data class Book(
  @Description("The title of the book.") val title: String,
  @Description("The author of the book.") val author: String,
  @Description("A 50 word summary of the book.") val summary: String
)

suspend fun main() {
  OpenAI.conversation {
    val toKillAMockingbird: Book = prompt("To Kill a Mockingbird by Harper Lee")
    println(toKillAMockingbird)
  }
}
