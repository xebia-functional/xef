package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable data class Person(val name: String, val age: Int)

suspend fun main() {
  OpenAI.conversation {
    val person: Person = prompt("What is your name and age?")
    println("Hello ${person.name}, you are ${person.age} years old.")
  }
}
