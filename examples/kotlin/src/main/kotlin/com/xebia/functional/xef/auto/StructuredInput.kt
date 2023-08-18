package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable data class Question(val question: String)

@Serializable data class Answer(val answer: String)

/** Demonstrates how to use any structured serializable input as a prompt. */
suspend fun main() {
  OpenAI.conversation {
    val question = Question("What is your name?")
    println("question: $question")
    val answer: Answer = prompt(question)
    println("answer: $answer")
  }
}
