package com.xebia.functional.xef.auto.pattern

import com.xebia.functional.xef.agents.jsonFormerPrompt
import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.auto.getOrElse
import kotlinx.serialization.Serializable

@Serializable
data class Book(
    val title: String,
    val author: String,
    val summary: String
)

suspend fun main() {
    ai {
        val toKillAMockingbird: Book = jsonFormerPrompt("To Kill a Mockingbird by Harper Lee summary.")
        println("To Kill a Mockingbird summary:\n ${toKillAMockingbird.summary}")
    }.getOrElse { println(it) }
}
