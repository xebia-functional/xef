package com.xebia.functional.xef.auto

import com.xebia.functional.xef.agents.search
import io.github.oshai.kotlinlogging.KotlinLogging

suspend fun main() {
    val logger = KotlinLogging.logger("Weather")

    val question = "Knowing this forecast, what clothes do you recommend I should wear?"
    val answer: List<String> = getQuestionAnswer(question)

    logger.info { answer }
}

private suspend fun getQuestionAnswer(
    question: String
): List<String> = ai {
    contextScope(search("Weather in Cádiz, Spain")) {
        promptMessage(question)
    }
}.getOrElse { throw IllegalStateException(it.reason) }
