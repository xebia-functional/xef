package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.getOrElse
import com.xebia.functional.xef.auto.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable
data class Book(
    @Description(["The title of the book"])
    val title: String,
    @Description(["The author of the book"])
    val author: String,
    @Description(["An extended summary of the book of at least 100 words"])
    val summary: String)

suspend fun main() {
    ai {
        val toKillAMockingbird: Book = prompt("To Kill a Mockingbird by Harper Lee summary.")
        println("To Kill a Mockingbird summary:\n ${toKillAMockingbird.summary}")
    }.getOrElse { println(it) }
}
