package com.xebia.functional.xef.auto

val lorem =
  "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."

fun generateRandomMessages(
  n: Int,
  letterInQuestion: Int = 0,
  letterInAnswer: Int = 0,
): Map<String, String> =
  (0 until n).associate {
    "question ${it + 1}${if (letterInQuestion > 0) ": ${lorem.substring(0, letterInQuestion)}" else ""}" to
      "answer ${it + 1}${if (letterInAnswer > 0) ": ${lorem.substring(0, letterInAnswer)}" else ""}"
  }
