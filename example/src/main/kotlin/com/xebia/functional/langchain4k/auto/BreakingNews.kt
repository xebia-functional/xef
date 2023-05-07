package com.xebia.functional.langchain4k.auto

import arrow.core.getOrElse
import com.xebia.functional.auto.ai
import com.xebia.functional.tool.search
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class BreakingNewsAboutCovid(
    val summary: String
)

suspend fun main() {
    ai {
        val sdf = SimpleDateFormat("dd/M/yyyy")
        val currentDate = sdf.format(Date())
        agent(
            *search("$currentDate Covid News")
        ) {
            val news: BreakingNewsAboutCovid = ai("$currentDate Covid News")
            println(news)
        }
    }.getOrElse { println(it) }
}

