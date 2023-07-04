package com.xebia.functional.xef.auto

import com.xebia.functional.xef.agents.search
import com.xebia.functional.xef.auto.llm.openai.getOrElse
import com.xebia.functional.xef.auto.llm.openai.promptMessage
import io.github.oshai.kotlinlogging.KotlinLogging

suspend fun main() {
    val logger = KotlinLogging.logger("Weather")

    val question = "Knowing this forecast, what clothes do you recommend I should wear?"
    val answer = getQuestionAnswer(question)

    logger.info { answer }
}

private suspend fun getQuestionAnswer(
    question: String
): String = ai {
    contextScope(search("Weather in Cádiz, Spain")) {
        promptMessage(question)
    }
}.getOrElse { throw IllegalStateException(it.message) }
