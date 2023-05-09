package com.xebia.functional.langchain4k.auto

import com.xebia.functional.auto.prompt
import com.xebia.functional.auto.getOrElse
import kotlinx.serialization.Serializable

@Serializable
data class Person(val name: String, val age: Int)

suspend fun main() {
    prompt {
        val person: Person = prompt("What is your name and age?")
        println("Hello ${person.name}, you are ${person.age} years old.")
    }.getOrElse { println(it) }
}
