package com.xebia.functional.langchain4k.auto

import arrow.core.getOrElse
import com.xebia.functional.auto.AI
import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class Book(val title: String, val author: String, val summary: String)

suspend fun main() {
    AI {
        val toKillAMockingbird: Book = ai("To Kill a Mockingbird by Harper Lee summary.")
        println("To Kill a Mockingbird summary:\n ${toKillAMockingbird.summary}")
    }.getOrElse { println(it) }
}
