package com.xebia.functional.examples.auto

import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class Person(val name: String, val age: Int)

suspend fun main() {
    val bogus: List<Person> = ai("come up with some random data that matches the type")
    println(bogus)
}
