package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.getOrElse
import com.xebia.functional.xef.auto.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable
data class ASCIIArt(val art: String)

suspend fun main() {
    val art: AI<ASCIIArt> = conversation {
        prompt( "ASCII art of a cat dancing")
    }
    println(art.getOrElse { ASCIIArt("¯\\_(ツ)_/¯" + "\n" + it.message) })
}
