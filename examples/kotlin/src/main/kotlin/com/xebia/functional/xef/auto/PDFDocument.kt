package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.getOrThrow
import com.xebia.functional.xef.auto.llm.openai.prompt
import com.xebia.functional.xef.pdf.pdf
import kotlinx.serialization.Serializable

@Serializable
data class AIResponse(val answer: String, val source: String)

const val pdfUrl = "https://people.cs.ksu.edu/~schmidt/705a/Scala/Programming-in-Scala.pdf"

suspend fun main() = ai {
  contextScope(pdf(url = pdfUrl)) {
    while (true) {
      print("Enter your question: ")
      val line = readlnOrNull() ?: break
      val response: AIResponse = prompt(line)
      println("${response.answer}\n---\n${response.source}\n---\n")
    }
  }
}.getOrThrow()
