package com.xebia.functional.auto

import kotlinx.serialization.Serializable

@Serializable
data class ASCIIArt(val art: String)

suspend fun main() {
    val art: ASCIIArt = ai("ASCII art of a cat dancing")
    println(art.art)
}
