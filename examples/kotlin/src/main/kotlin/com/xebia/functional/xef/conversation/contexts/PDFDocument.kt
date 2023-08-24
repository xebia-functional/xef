package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.prompt
import com.xebia.functional.xef.pdf.pdf
import kotlinx.serialization.Serializable

@Serializable data class AIResponse(val answer: String, val source: String)

const val pdfUrl = "https://people.cs.ksu.edu/~schmidt/705a/Scala/Programming-in-Scala.pdf"

suspend fun main() =
  OpenAI.conversation {
    addContext(pdf(url = pdfUrl))
    while (true) {
      print("Enter your question: ")
      val line = readlnOrNull() ?: break
      val response: AIResponse = prompt(line)
      println("${response.answer}\n---\n${response.source}\n---\n")
    }
  }
