package com.xebia.functional.xef.auto

import com.xebia.functional.xef.pdf.pdf
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class AIResponse(val answer: String, val source: String)

suspend fun main() = ai {
  val file = AIResponse::class.java.getResource("/documents/doc.pdf").file
  contextScope(pdf(file = File(file))) {
    while (true) {
      print("Enter your question: ")
      val line = readlnOrNull() ?: break
      val response: AIResponse = prompt(line)
      println("${response.answer}\n---\n${response.source}\n---\n")
    }
  }
}.getOrThrow()
