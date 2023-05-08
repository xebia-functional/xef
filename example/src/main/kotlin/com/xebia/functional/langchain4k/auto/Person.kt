package com.xebia.functional.langchain4k.auto

import com.xebia.functional.auto.ai
import com.xebia.functional.auto.getOrElse
import kotlinx.serialization.Serializable

@Serializable
data class Person(val name: String, val age: Int)

suspend fun main() {
    ai {
        val person: Person = ai("What is your name and age?")
        println("Hello ${person.name}, you are ${person.age} years old.")
    }.getOrElse { println(it) }
}
