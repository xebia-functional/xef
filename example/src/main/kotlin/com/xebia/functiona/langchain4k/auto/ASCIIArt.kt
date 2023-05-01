package com.xebia.functiona.langchain4k.auto

import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class ASCIIArt(val art: String)

suspend fun main() {
    val art: ASCIIArt = ai("ASCII art of a cat dancing")
    println(art.art)
}
