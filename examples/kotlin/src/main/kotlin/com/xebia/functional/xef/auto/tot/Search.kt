package com.xebia.functional.xef.auto.tot

import com.xebia.functional.xef.auto.AIScope

suspend fun AIScope.generateSearchPrompts(problem: Problem): List<String> =
  promptMessage(
    """|You are an expert SEO consultant.
    |You generate search prompts for a problem.
    |You are given the following problem:
    |${problem.description}
    |Instructions:
    |1. Generate 1 search prompt to get the best results for this problem.
    |2. Ensure the search prompt are relevant to the problem.
    |3. Ensure the search prompt expands into the keywords needed to solve the problem.
    |
  """.trimMargin(),
    n = 5
  ).distinct().map { it.replace("\"", "").trim() }

