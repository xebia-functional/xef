package com.xebia.functional.xef.auto.pattern

import com.xebia.functional.xef.agents.patternPrompt
import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.auto.getOrElse
import kotlinx.serialization.json.Json

suspend fun main() {
    val enableComparison = false

    ai {
        val goal = "Return the first three letters of the alphabet in the format of a JSON array: "
        val pattern = Regex("""\["[a-z]", "[a-z]", "[a-z]"]""")

        val pessimistic: String = patternPrompt(
            prompt = goal,
            pattern = pattern,
            maxIterations = 10,
            maxTokensPerCompletion = 3
        )
        val pessimisticDecoded: List<String> = Json.decodeFromString(pessimistic)
        println(pessimisticDecoded)

        if (enableComparison) {
            val optimistic: String = patternPrompt(
                prompt = goal,
                pattern = pattern,
                maxIterations = 10,
                maxTokensPerCompletion = 3
            )
            val optimisticDecoded: List<String> = Json.decodeFromString(optimistic)
            println(optimisticDecoded)
        }
    }.getOrElse { println(it) }
}
