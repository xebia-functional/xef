package com.xebia.functional.xef.auto.pattern

import com.xebia.functional.xef.agents.patternPromptOptimistic
import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.auto.getOrElse
import com.xebia.functional.xef.auto.prompt
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

suspend fun main() {
    val enableComparison = false

    ai {
        val goal = "Return the first three letters of the alphabet in a JSON array: "
        val patternResponse: String = patternPromptOptimistic(
            prompt = goal,
            pattern = Regex("""\["[a-z]", "[a-z]", "[a-z]"]"""),
            maxIterations = 20
        )
        val list: List<String> = Json.decodeFromString(patternResponse)
        println(list)

        if (enableComparison) {
            val response: List<String> = prompt(goal)
            println(response)
        }

    }.getOrElse { println(it) }
}
