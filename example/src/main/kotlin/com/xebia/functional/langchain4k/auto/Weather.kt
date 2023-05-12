package com.xebia.functional.langchain4k.auto

import com.xebia.functional.auto.ai
import com.xebia.functional.auto.getOrElse
import com.xebia.functional.agents.search
import io.github.oshai.KotlinLogging

suspend fun main() {
    val logger = KotlinLogging.logger("Weather")

    val question = "Knowing this forecast, what clothes do you recommend I should wear?"
    val answer: List<String> = getQuestionAnswer(question)

    logger.info { answer }
}

private suspend fun getQuestionAnswer(
    question: String
): List<String> = ai {
    context(search("Weather in Cádiz, Spain")) {
        promptMessage(question)
    }
}.getOrElse { throw IllegalStateException(it.reason) }
