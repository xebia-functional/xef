package com.xebia.functional.xef.auto

import com.xebia.functional.auto.AI
import com.xebia.functional.auto.ai
import com.xebia.functional.auto.getOrElse
import kotlinx.serialization.Serializable

@Serializable
data class ASCIIArt(val art: String)

suspend fun main() {
    val art: AI<ASCIIArt> = ai {
        prompt("ASCII art of a cat dancing")
    }
    println(art.getOrElse { ASCIIArt("¯\\_(ツ)_/¯" + "\n" + it.reason) })
}
