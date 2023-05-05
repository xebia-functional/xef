package com.xebia.functional.langchain4k.auto

import arrow.core.getOrElse
import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class Person(val name: String, val age: Int)

suspend fun main() {
    ai {
        val person: Person = ai("What is your name and age?")
        println("Hello ${person.name}, you are ${person.age} years old.")
    }.getOrElse { println(it) }
}
