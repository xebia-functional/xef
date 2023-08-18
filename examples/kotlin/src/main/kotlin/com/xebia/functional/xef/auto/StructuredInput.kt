package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.prompt
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.user
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable data class Question(val question: String)

@Serializable data class Answer(val answer: String)

/** Demonstrates how to use any structured serializable input as a prompt. */
suspend fun main() {
  OpenAI.conversation {
    val question = Question("What is your name?")
    println("question: $question")
    val answer: Answer = prompt(Prompt { +user(question, serializer<Question>()) })
    println("answer: $answer")
  }
}
