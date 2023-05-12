package com.xebia.functional.langchain4k.auto

import com.xebia.functional.auto.ai
import com.xebia.functional.auto.getOrElse
import com.xebia.functional.tool.search
import io.github.oshai.KotlinLogging

suspend fun main() {
    val logger = KotlinLogging.logger("Weather")

    val question = "Knowing this forecast, what clothes do you recommend I should wear?"
    val answer: Map<String, String> = getQuestionAnswer(question)

    logger.info { answer }
}

private suspend fun getQuestionAnswer(
    question: String
): Map<String, String> = ai<Map<String, String>> {
    context(search("Weather in Cádiz, Spain")) {
        prompt(question)
    }
}.getOrElse { throw IllegalStateException(it.reason) }
