package com.xebia.functional.xef.conversation.contexts

import ai.xef.openai.StandardModel
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.pdf.pdf
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.user
import kotlinx.serialization.Serializable

@Serializable data class AIResponse(val answer: String, val source: String)

const val pdfUrl = "https://people.cs.ksu.edu/~schmidt/705a/Scala/Programming-in-Scala.pdf"

suspend fun main() = Conversation {
  addContext(pdf(url = pdfUrl))
  while (true) {
    print("Enter your question: ")
    val line = readlnOrNull() ?: break
    val model = StandardModel(CreateChatCompletionRequestModel.gpt_3_5_turbo_0613)
    val response: AIResponse = prompt(Prompt(model) { +user(line) })
    println("${response.answer}\n---\n${response.source}\n---\n")
  }
}
