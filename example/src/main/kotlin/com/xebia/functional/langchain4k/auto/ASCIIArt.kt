package com.xebia.functional.langchain4k.auto

import arrow.core.Either
import arrow.core.getOrElse
import com.xebia.functional.auto.AIError
import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class ASCIIArt(val art: String)

suspend fun main() {
    val art: Either<AIError, ASCIIArt> = ai {
        ai("ASCII art of a cat dancing")
    }
    println(art.getOrElse { ASCIIArt("¯\\_(ツ)_/¯") })
}
