package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.getOrElse
import com.xebia.functional.xef.auto.llm.openai.image
import kotlinx.serialization.Serializable

@Serializable
data class Population(val size: Int, val description: String)

@Serializable
data class Image(
    val description: String,
    val url: String,
)

suspend fun main() =
    ai {
        val img: Image = image("")
        println(img)
    }.getOrElse { println(it) }
