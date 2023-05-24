package com.xebia.functional.xef.auto

import com.xebia.functional.xef.agents.patternPrompt

suspend fun main() {
    ai {
        val goal = "Return the first three letters of the alphabet in a json array: "
        val patternResponse: String = patternPrompt(
            prompt = goal,
            pattern = Regex("""\["[a-z]", "[a-z]", "[a-z]"]"""),
            maxNewTokens = 20
        )
        println(patternResponse)

        val response: String = prompt(goal)
        println(response)

    }.getOrElse { println(it) }
}
