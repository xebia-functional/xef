package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.getOrElse
import com.xebia.functional.xef.auto.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable
data class Person(val name: String, val age: Int)

suspend fun main() {
    ai {
        val person: Person = prompt("What is your name and age?")
        println("Hello ${person.name}, you are ${person.age} years old.")
    }.getOrElse { println(it) }
}
